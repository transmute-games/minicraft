package minicraft.network;

import org.junit.Assert;
import org.junit.Test;

/** Two in-process clients + server: join, move, dig, pickup, chest, disconnect. */
public class MultiplayerIntegrationTest {
  @Test
  public void lanJoinMoveDigPickupChestDisconnect() throws Exception {
    int port = 18225;
    try (GameServer server = new GameServer(port)) {
      server.start();
      Thread.sleep(150);

      try (GameClient a = new GameClient("127.0.0.1", port);
          GameClient b = new GameClient("127.0.0.1", port)) {
        a.login("Alice");
        Assert.assertTrue(a.awaitInit(3000));
        b.login("Bob");
        Assert.assertTrue(b.awaitInit(3000));

        Assert.assertNotEquals(a.playerId(), b.playerId());

        a.move(12, 14);
        Thread.sleep(150);
        Assert.assertEquals(12, a.x());
        Assert.assertEquals(14, a.y());
        Assert.assertEquals(12, server.players().get(a.playerId()).x);

        Assert.assertEquals(Integer.valueOf(1), server.tileAt(10, 10));
        a.dig(10, 10);
        Thread.sleep(150);
        Assert.assertEquals(Integer.valueOf(0), server.tileAt(10, 10));
        Assert.assertEquals(Integer.valueOf(0), b.tile(10, 10));

        a.pickup(3);
        Thread.sleep(100);
        Assert.assertEquals(3, a.wood());

        a.chestIn();
        Thread.sleep(100);
        Assert.assertEquals(1, a.chest());
        Assert.assertEquals(2, a.wood());

        a.chestOut();
        Thread.sleep(100);
        Assert.assertEquals(0, a.chest());
        Assert.assertEquals(3, a.wood());

        int bobId = b.playerId();
        b.disconnect();
        Thread.sleep(150);
        Assert.assertFalse(server.players().containsKey(bobId));
      }
    }
  }
}
