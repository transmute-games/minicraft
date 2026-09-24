package minicraft.network;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight LAN client for the {@link GameServer} protocol.
 */
public final class GameClient implements AutoCloseable
{
    private final PacketConnection conn;
    private final AtomicInteger playerId = new AtomicInteger(-1);
    private final AtomicInteger x = new AtomicInteger(0);
    private final AtomicInteger y = new AtomicInteger(0);
    private final AtomicInteger wood = new AtomicInteger(0);
    private final AtomicInteger chest = new AtomicInteger(0);
    private final Map<Integer, Integer> tiles = new ConcurrentHashMap<>();
    private final Map<Integer, String> remotePlayers = new ConcurrentHashMap<>();
    private final CountDownLatch inited = new CountDownLatch(1);

    public GameClient(String host, int port) throws IOException
    {
        Socket socket = new Socket(host, port);
        conn = new PacketConnection(socket);
        conn.startReader(this::onPacket);
    }

    private void onPacket(MinicraftProtocol.InputType type, String payload)
    {
        switch (type)
        {
            case INIT -> {
                String[] p = payload.split(";");
                playerId.set(Integer.parseInt(p[0]));
                x.set(Integer.parseInt(p[1]));
                y.set(Integer.parseInt(p[2]));
                inited.countDown();
            }
            case MOVE -> {
                String[] p = payload.split(";");
                if (p.length >= 3 && Integer.parseInt(p[0]) == playerId.get())
                {
                    x.set(Integer.parseInt(p[1]));
                    y.set(Integer.parseInt(p[2]));
                }
            }
            case TILE -> {
                String[] p = payload.split(";");
                if (p.length >= 3)
                {
                    int tx = Integer.parseInt(p[0]);
                    int ty = Integer.parseInt(p[1]);
                    tiles.put((tx << 16) | (ty & 0xFFFF), Integer.parseInt(p[2]));
                }
            }
            case ADD -> {
                String[] p = payload.split(";");
                if (p.length >= 2)
                {
                    remotePlayers.put(Integer.parseInt(p[0]), p[1]);
                }
            }
            case REMOVE -> remotePlayers.remove(Integer.parseInt(payload.trim()));
            case ADDITEMS -> {
                String[] p = payload.split(";");
                if (p.length >= 2)
                {
                    wood.set(Integer.parseInt(p[1]));
                }
            }
            case CHESTIN -> {
                String[] p = payload.split(";");
                if (p.length >= 2)
                {
                    chest.set(Integer.parseInt(p[1]));
                    wood.decrementAndGet();
                }
            }
            case CHESTOUT -> {
                String[] p = payload.split(";");
                if (p.length >= 2)
                {
                    wood.set(Integer.parseInt(p[1]));
                    chest.decrementAndGet();
                }
            }
            default -> {
            }
        }
    }

    public boolean awaitInit(long ms) throws InterruptedException
    {
        return inited.await(ms, TimeUnit.MILLISECONDS);
    }

    public void login(String name)
    {
        conn.send(MinicraftProtocol.InputType.LOGIN, name);
    }

    public void move(int nx, int ny)
    {
        x.set(nx);
        y.set(ny);
        conn.send(MinicraftProtocol.InputType.MOVE, nx + ";" + ny);
    }

    public void dig(int tx, int ty)
    {
        conn.send(MinicraftProtocol.InputType.TILE, tx + ";" + ty);
    }

    public void pickup(int amount)
    {
        conn.send(MinicraftProtocol.InputType.PICKUP, String.valueOf(amount));
    }

    public void chestIn()
    {
        conn.send(MinicraftProtocol.InputType.CHESTIN, "");
    }

    public void chestOut()
    {
        conn.send(MinicraftProtocol.InputType.CHESTOUT, "");
    }

    public void disconnect()
    {
        conn.send(MinicraftProtocol.InputType.DISCONNECT, "");
    }

    public int playerId()
    {
        return playerId.get();
    }

    public int x()
    {
        return x.get();
    }

    public int y()
    {
        return y.get();
    }

    public int wood()
    {
        return wood.get();
    }

    public int chest()
    {
        return chest.get();
    }

    public Integer tile(int tx, int ty)
    {
        return tiles.get((tx << 16) | (ty & 0xFFFF));
    }

    public Map<Integer, String> remotes()
    {
        return remotePlayers;
    }

    @Override
    public void close()
    {
        disconnect();
        conn.close();
    }
}
