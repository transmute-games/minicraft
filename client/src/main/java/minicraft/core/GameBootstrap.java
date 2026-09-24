package minicraft.core;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.level.tile.Tiles;
import minicraft.network.Analytics;
import minicraft.saveload.Load;
import minicraft.screen.ResourcePackDisplay;
import minicraft.screen.TitleDisplay;

/**
 * Boots Minicraft domain systems without owning a window/loop (Transmute does that).
 */
public final class GameBootstrap
{
    private static boolean booted;

    private GameBootstrap()
    {
    }

    public static synchronized void bootstrap(boolean headless)
    {
        if (booted)
        {
            return;
        }
        booted = true;
        System.setProperty("minicraft.disableControllers", "true");

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler::crashHandle);

        Initializer.parseArgs(new String[]{});

        try
        {
            Analytics.GameStartup.ping();
        }
        catch (Throwable ignored)
        {
            // Analytics is best-effort offline.
        }

        new Load(true, true);
        if (!headless && (boolean) Settings.get("hwa"))
        {
            System.setProperty("sun.java2d.opengl", "true");
        }
        Game.MAX_FPS = (int) Settings.get("fps");

        Game.input = new InputHandler(); // No AWT Component; Transmute InputBridge drives keys.

        ResourcePackDisplay.initPacks();
        ResourcePackDisplay.reloadResources();

        Tiles.initTileList();

        Game.setDisplay(new TitleDisplay());

        Renderer.initScreen(headless);

        World.resetGame();
        Game.player.eid = 0;
        new Load(true, false);
    }
}
