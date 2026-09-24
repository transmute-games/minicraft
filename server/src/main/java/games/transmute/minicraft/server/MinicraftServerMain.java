package games.transmute.minicraft.server;

import minicraft.network.GameServer;
import minicraft.network.MinicraftProtocol;

/**
 * Dedicated headless server entrypoint.
 */
public final class MinicraftServerMain
{
    public static void main(String[] args) throws Exception
    {
        int port = MinicraftProtocol.PORT;
        for (int i = 0; i < args.length; i++)
        {
            if ("--port".equals(args[i]) && i + 1 < args.length)
            {
                port = Integer.parseInt(args[++i]);
            }
        }
        GameServer server = new GameServer(port);
        server.start();
        System.out.println("Minicraft server listening on port " + port);
        Thread.currentThread().join();
    }
}
