package games.transmute.minicraft;

import TransmuteCore.assets.AssetPack;
import TransmuteCore.core.GameConfig;
import TransmuteCore.core.Manager;
import TransmuteCore.core.TransmuteCore;
import TransmuteCore.core.interfaces.services.IRenderer;
import TransmuteCore.graphics.Context;
import TransmuteCore.util.verify.FrameAssert;
import TransmuteCore.util.verify.GameHarness;
import minicraft.core.Game;
import minicraft.core.GameBootstrap;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.InputBridge;

/**
 * Transmute-backed entry for Minicraft+. Owns the window/loop; Minicraft domain ticks and paints.
 */
public final class MinicraftGame extends TransmuteCore
{
    public static final int VIEW_W = Renderer.WIDTH;
    public static final int VIEW_H = Renderer.HEIGHT;
    public static final int SCALE = 3;

    private final boolean headless;
    private double tickAccumulator;

    public MinicraftGame(GameConfig config)
    {
        super(config);
        this.headless = config.isHeadless();
    }

    @Override
    public void init()
    {
        getManager().bootstrapDefaults();
        AssetPack.create(getManager().getAssetManager())
            .font(AssetPack.DEFAULT_FONT_RESOURCE)
            .ensureDefaultFont();

        GameBootstrap.bootstrap(headless);
    }

    @Override
    public void update(Manager manager, double delta)
    {
        InputBridge.syncFromTransmute(manager.getInputHandler());

        // Fixed 60 Hz Minicraft ticks (Updater.normSpeed), independent of render FPS.
        tickAccumulator += delta;
        double tickSeconds = 1.0 / Updater.normSpeed;
        int safety = 0;
        while (tickAccumulator >= tickSeconds && safety++ < 5)
        {
            Updater.tick();
            tickAccumulator -= tickSeconds;
        }
    }

    @Override
    public void render(Manager manager, IRenderer renderer)
    {
        Renderer.renderFrame();
        Context ctx = (Context) renderer;
        int[] src = Renderer.pixelBuffer();
        int[] dst = ctx.getPixels();
        if (src == null || dst == null)
        {
            return;
        }
        int n = Math.min(src.length, dst.length);
        for (int i = 0; i < n; i++)
        {
            // Minicraft buffer is TYPE_INT_RGB; Context expects ARGB.
            dst[i] = 0xFF000000 | (src[i] & 0x00FFFFFF);
        }
    }

    public static GameConfig config(boolean headless)
    {
        return new GameConfig.Builder()
            .title(Game.NAME)
            .version(Game.VERSION.toString())
            .size(VIEW_W * SCALE, VIEW_H * SCALE)
            .scale(SCALE)
            .headless(headless)
            .showStartScreen(false)
            .build();
    }

    public static void main(String[] args)
    {
        boolean headless = false;
        for (String arg : args)
        {
            if ("--headless".equals(arg))
            {
                headless = true;
            }
        }

        if (headless)
        {
            try (GameHarness harness = GameHarness.of(() -> new MinicraftGame(config(true))))
            {
                harness.step(2);
                // Title screen should paint non-black pixels somewhere in the buffer.
                Context ctx = (Context) harness.renderer();
                int[] px = ctx.getPixels();
                boolean anyLit = false;
                for (int p : px)
                {
                    if ((p & 0x00FFFFFF) != 0)
                    {
                        anyLit = true;
                        break;
                    }
                }
                if (!anyLit)
                {
                    throw new AssertionError("Headless smoke: expected title screen pixels");
                }
                FrameAssert.assertPixel(harness.renderer(), 0, 0, px[0]);
                System.out.println("verifyHeadless OK");
            }
            return;
        }

        MinicraftGame game = new MinicraftGame(config(false));
        game.start();
    }
}
