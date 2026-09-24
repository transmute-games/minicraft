package games.transmute.minicraft;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.item.StackableItem;
import minicraft.level.Level;
import minicraft.saveload.Save;
import minicraft.screen.WorldGenDisplay;
import minicraft.screen.WorldSelectDisplay;

import java.io.File;
import java.nio.file.Files;

/**
 * Headless helpers for singleplayer vertical-slice playtests.
 */
public final class SinglePlayerSession
{
    public static final long TEST_SEED = 42L;
    public static final String TEST_WORLD = "transmute-playtest";

    private SinglePlayerSession()
    {
    }

    /** Generate a small survival world and enter gameplay (no menus). */
    public static void startFreshWorld()
    {
        Settings.set("size", 128);
        Settings.set("mode", "minicraft.settings.mode.survival");
        WorldSelectDisplay.setWorldName(TEST_WORLD, false);
        WorldGenDisplay.setSeedInput(String.valueOf(TEST_SEED));
        World.setWorldSeed(TEST_SEED);
        World.initWorld();
        Game.setDisplay(null);
        // Ensure Updater drops any lingering menu reference on next ticks.
        for (int i = 0; i < 3; i++) {
            minicraft.core.Updater.tick();
        }
        Game.setDisplay(null);
        Renderer.readyToRenderGameplay = true;
        if (Game.getDisplay() != null) {
            throw new IllegalStateException("Expected no display after world start, got " + Game.getDisplay());
        }
        if (Game.player.getLevel() == null) {
            throw new IllegalStateException("Player has no level after world start");
        }
    }

    public static Player player()
    {
        return Game.player;
    }

    public static Level surface()
    {
        return Game.levels[Game.currentLevel];
    }

    public static void giveWood(int count)
    {
        Item wood = Items.get("Wood");
        if (wood instanceof StackableItem)
        {
            StackableItem stack = (StackableItem) wood.copy();
            stack.count = count;
            player().getInventory().add(stack);
        }
        else
        {
            for (int i = 0; i < count; i++)
            {
                player().getInventory().add(wood.copy());
            }
        }
    }

    public static boolean craftPlanks()
    {
        Recipe recipe = new Recipe("plank_2", "Wood_1");
        return recipe.craft(player());
    }

    public static File saveWorld()
    {
        new Save(WorldSelectDisplay.getWorldName());
        return new File(Game.gameDir + "/saves/" + WorldSelectDisplay.getWorldName());
    }

    public static void loadWorld()
    {
        WorldSelectDisplay.setWorldName(TEST_WORLD, true);
        World.initWorld();
        Game.setDisplay(null);
        Renderer.readyToRenderGameplay = true;
    }

    public static void clearTestSave()
    {
        try
        {
            File dir = new File(Game.gameDir + "/saves/" + TEST_WORLD);
            if (dir.exists())
            {
                Files.walk(dir.toPath())
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> path.toFile().delete());
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
