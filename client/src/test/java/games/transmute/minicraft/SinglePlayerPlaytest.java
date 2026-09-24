package games.transmute.minicraft;

import TransmuteCore.input.SimulatedInput;
import TransmuteCore.util.verify.GameHarness;
import minicraft.core.Renderer;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.screen.WorldGenDisplay;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Vertical-slice playtests: gen, move, dig, craft, save/load.
 */
public class SinglePlayerPlaytest
{
    @Before
    public void setUp()
    {
        SinglePlayerSession.clearTestSave();
    }

    @Test
    public void worldGenMoveDigCraftSaveLoad()
    {
        try (GameHarness harness = GameHarness.of(() -> new MinicraftGame(MinicraftGame.config(true))))
        {
            harness.step(1);

            WorldGenDisplay.setSeedInput(String.valueOf(SinglePlayerSession.TEST_SEED));
            SinglePlayerSession.startFreshWorld();

            Player player = SinglePlayerSession.player();
            Assert.assertNotNull(player);
            Assert.assertNotNull(SinglePlayerSession.surface());
            Assert.assertTrue(Renderer.readyToRenderGameplay);

            int startX = player.x;
            int startY = player.y;

            SimulatedInput input = (SimulatedInput) harness.game().getManager().getInputHandler();
            input.pressKey(KeyEvent.VK_D);
            for (int i = 0; i < 30; i++)
            {
                harness.step(1);
            }
            input.releaseKey(KeyEvent.VK_D);
            harness.step(2);

            Assert.assertTrue("player should move when holding D",
                player.x != startX || player.y != startY);

            // Attack once: click SPACE, step once, assert stamina spent before recharge.
            int staminaBefore = player.stamina;
            input.pressKey(KeyEvent.VK_SPACE);
            harness.step(1);
            Assert.assertTrue("attack should spend stamina on click", player.stamina < staminaBefore);
            input.releaseKey(KeyEvent.VK_SPACE);
            harness.step(1);

            // Dig: place rock and mine it with damage until destroyed.
            Level level = SinglePlayerSession.surface();
            int tx = player.x >> 4;
            int ty = (player.y >> 4) + 1;
            level.setTile(tx, ty, Tiles.get("Rock"));
            Tile rock = level.getTile(tx, ty);
            for (int i = 0; i < 80; i++)
            {
                rock.hurt(level, tx, ty, 50);
                rock = level.getTile(tx, ty);
                if (rock != Tiles.get("Rock"))
                {
                    break;
                }
            }
            Assert.assertNotEquals("rock should be mined away", Tiles.get("Rock").id, level.getTile(tx, ty).id);

            SinglePlayerSession.giveWood(5);
            Assert.assertTrue(player.getInventory().count(Items.get("Wood")) >= 1);
            Assert.assertTrue(SinglePlayerSession.craftPlanks());
            int planks = player.getInventory().count(Items.get("plank"));
            if (planks < 1)
            {
                planks = player.getInventory().count(Items.get("Plank"));
            }
            Assert.assertTrue(planks >= 1);

            File saveDir = SinglePlayerSession.saveWorld();
            Assert.assertTrue("save directory exists", saveDir.exists());

            int savedX = player.x;
            int savedY = player.y;
            SinglePlayerSession.loadWorld();
            harness.step(2);
            Player loaded = SinglePlayerSession.player();
            Assert.assertNotNull(loaded);
            Assert.assertEquals(savedX, loaded.x);
            Assert.assertEquals(savedY, loaded.y);
        }
    }
}
