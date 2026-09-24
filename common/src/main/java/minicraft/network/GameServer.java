package minicraft.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Authoritative LAN session: LOGIN, MOVE, TILE dig, PICKUP, CHESTIN/OUT, DISCONNECT. Keeps a simple
 * shared tile+player state suitable for headless integration tests.
 */
public final class GameServer implements AutoCloseable {
  public static final class PlayerState {
    public final int id;
    public final String name;
    public int x;
    public int y;
    public int wood;
    public int chestItems;

    PlayerState(int id, String name, int x, int y) {
      this.id = id;
      this.name = name;
      this.x = x;
      this.y = y;
    }
  }

  private final int port;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicInteger nextId = new AtomicInteger(1);
  private final Map<Integer, PlayerState> players = new ConcurrentHashMap<>();
  private final Map<Integer, PacketConnection> connections = new ConcurrentHashMap<>();

  /** Sparse tile map: key = (tx<<16)|ty , value = tileId (0=empty/dug). */
  private final Map<Integer, Integer> tiles = new ConcurrentHashMap<>();

  private ServerSocket serverSocket;
  private Thread acceptThread;

  public GameServer(int port) {
    this.port = port;
    // Seed a diggable rock at (10,10)
    tiles.put(pack(10, 10), 1);
  }

  public void start() throws IOException {
    serverSocket = new ServerSocket(port);
    running.set(true);
    acceptThread = new Thread(this::acceptLoop, "minicraft-server-accept");
    acceptThread.setDaemon(true);
    acceptThread.start();
  }

  private void acceptLoop() {
    while (running.get()) {
      try {
        Socket socket = serverSocket.accept();
        PacketConnection conn = new PacketConnection(socket);
        conn.startReader((type, payload) -> handle(conn, type, payload));
      } catch (IOException e) {
        if (running.get()) {
          e.printStackTrace();
        }
      }
    }
  }

  private void handle(PacketConnection conn, MinicraftProtocol.InputType type, String payload) {
    switch (type) {
      case LOGIN -> onLogin(conn, payload);
      case MOVE -> onMove(conn, payload);
      case TILE, INTERACT -> onDig(conn, payload);
      case PICKUP -> onPickup(conn, payload);
      case CHESTIN -> onChestIn(conn);
      case CHESTOUT -> onChestOut(conn);
      case DISCONNECT -> onDisconnect(conn);
      case PING -> conn.send(MinicraftProtocol.InputType.PING, "pong");
      default -> {}
    }
  }

  private void onLogin(PacketConnection conn, String name) {
    int id = nextId.getAndIncrement();
    PlayerState p =
        new PlayerState(id, name == null || name.isEmpty() ? "Player" + id : name, 8, 8);
    players.put(id, p);
    connections.put(id, conn);
    conn.send(MinicraftProtocol.InputType.INIT, id + ";" + p.x + ";" + p.y);
    broadcast(MinicraftProtocol.InputType.ADD, id + ";" + p.name + ";" + p.x + ";" + p.y, id);
    // Send known tiles
    tiles.forEach(
        (k, v) ->
            conn.send(MinicraftProtocol.InputType.TILE, unpackX(k) + ";" + unpackY(k) + ";" + v));
  }

  private Integer idOf(PacketConnection conn) {
    for (Map.Entry<Integer, PacketConnection> e : connections.entrySet()) {
      if (e.getValue() == conn) {
        return e.getKey();
      }
    }
    return null;
  }

  private void onMove(PacketConnection conn, String payload) {
    Integer id = idOf(conn);
    if (id == null) {
      return;
    }
    String[] parts = payload.split(";");
    if (parts.length < 2) {
      return;
    }
    PlayerState p = players.get(id);
    p.x = Integer.parseInt(parts[0]);
    p.y = Integer.parseInt(parts[1]);
    broadcast(MinicraftProtocol.InputType.MOVE, id + ";" + p.x + ";" + p.y, -1);
  }

  private void onDig(PacketConnection conn, String payload) {
    Integer id = idOf(conn);
    if (id == null) {
      return;
    }
    String[] parts = payload.split(";");
    if (parts.length < 2) {
      return;
    }
    int tx = Integer.parseInt(parts[0]);
    int ty = Integer.parseInt(parts[1]);
    tiles.put(pack(tx, ty), 0);
    broadcast(MinicraftProtocol.InputType.TILE, tx + ";" + ty + ";0", -1);
  }

  private void onPickup(PacketConnection conn, String payload) {
    Integer id = idOf(conn);
    if (id == null) {
      return;
    }
    PlayerState p = players.get(id);
    int amt = 1;
    try {
      if (payload != null && !payload.isEmpty()) {
        amt = Integer.parseInt(payload.trim());
      }
    } catch (NumberFormatException ignored) {
    }
    p.wood += amt;
    conn.send(MinicraftProtocol.InputType.ADDITEMS, "Wood;" + p.wood);
  }

  private void onChestIn(PacketConnection conn) {
    Integer id = idOf(conn);
    if (id == null) {
      return;
    }
    PlayerState p = players.get(id);
    if (p.wood > 0) {
      p.wood--;
      p.chestItems++;
      conn.send(MinicraftProtocol.InputType.CHESTIN, "Wood;" + p.chestItems);
    }
  }

  private void onChestOut(PacketConnection conn) {
    Integer id = idOf(conn);
    if (id == null) {
      return;
    }
    PlayerState p = players.get(id);
    if (p.chestItems > 0) {
      p.chestItems--;
      p.wood++;
      conn.send(MinicraftProtocol.InputType.CHESTOUT, "Wood;" + p.wood);
    }
  }

  private void onDisconnect(PacketConnection conn) {
    Integer id = idOf(conn);
    if (id == null) {
      return;
    }
    players.remove(id);
    connections.remove(id);
    broadcast(MinicraftProtocol.InputType.REMOVE, String.valueOf(id), id);
    conn.close();
  }

  private void broadcast(MinicraftProtocol.InputType type, String payload, int exceptId) {
    for (Map.Entry<Integer, PacketConnection> e : connections.entrySet()) {
      if (e.getKey() != exceptId) {
        e.getValue().send(type, payload);
      }
    }
  }

  public Map<Integer, PlayerState> players() {
    return players;
  }

  public Integer tileAt(int tx, int ty) {
    return tiles.get(pack(tx, ty));
  }

  public int getPort() {
    return port;
  }

  private static int pack(int tx, int ty) {
    return (tx << 16) | (ty & 0xFFFF);
  }

  private static int unpackX(int key) {
    return key >> 16;
  }

  private static int unpackY(int key) {
    return key & 0xFFFF;
  }

  @Override
  public void close() {
    running.set(false);
    connections.values().forEach(PacketConnection::close);
    connections.clear();
    players.clear();
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException ignored) {
      }
    }
  }
}
