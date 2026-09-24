package minicraft.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/** Null-delimited packet stream over TCP (adapted from Minicraft+ Online framing). */
public final class PacketConnection implements AutoCloseable {
  private final Socket socket;
  private final BufferedReader in;
  private final PrintWriter out;
  private final AtomicBoolean open = new AtomicBoolean(true);
  private Thread reader;

  public PacketConnection(Socket socket) throws IOException {
    this.socket = Objects.requireNonNull(socket, "socket");
    this.in =
        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
  }

  public void send(MinicraftProtocol.InputType type, String payload) {
    if (!open.get()) {
      return;
    }
    String body = payload == null ? "" : payload;
    out.print(MinicraftProtocol.encodeType(type));
    out.print(body);
    out.print('\0');
    out.flush();
  }

  public void startReader(BiConsumer<MinicraftProtocol.InputType, String> handler) {
    reader =
        new Thread(
            () -> {
              StringBuilder buf = new StringBuilder();
              try {
                while (open.get() && !socket.isClosed()) {
                  int read = in.read();
                  if (read < 0) {
                    break;
                  }
                  if (read > 0) {
                    buf.append((char) read);
                  } else if (buf.length() > 0) {
                    MinicraftProtocol.InputType type =
                        MinicraftProtocol.getInputType(buf.charAt(0));
                    String payload = buf.substring(1);
                    buf.setLength(0);
                    if (type != null) {
                      handler.accept(type, payload);
                    }
                  }
                }
              } catch (IOException ignored) {
              } finally {
                open.set(false);
              }
            },
            "minicraft-net-reader");
    reader.setDaemon(true);
    reader.start();
  }

  public boolean isOpen() {
    return open.get() && !socket.isClosed();
  }

  @Override
  public void close() {
    open.set(false);
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }
}
