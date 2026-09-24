package games.transmute.minicraft;

import TransmuteCore.assets.AssetPack;
import TransmuteCore.core.GameConfig;
import TransmuteCore.core.Manager;
import TransmuteCore.core.TransmuteCore;
import TransmuteCore.core.interfaces.services.IRenderer;
import TransmuteCore.graphics.Context;
import TransmuteCore.util.verify.FrameAssert;
import TransmuteCore.util.verify.GameHarness;
import minicraft.core.BootSplash;
import minicraft.core.Game;
import minicraft.core.GameBootstrap;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.io.InputBridge;

/**
 * Transmute-backed entry for Minicraft+. Owns the window/loop; Minicraft domain ticks and paints.
 */
public final class MinicraftGame extends TransmuteCore {
  public static final int VIEW_W = Renderer.WIDTH;
  public static final int VIEW_H = Renderer.HEIGHT;
  public static final int SCALE = 3;

  private final boolean headless;
  private double tickAccumulator;

  public MinicraftGame(GameConfig config) {
    super(config);
    this.headless = config.isHeadless();
  }

  @Override
  public void init() {
    getManager().bootstrapDefaults();
    AssetPack.create(getManager().getAssetManager())
        .font(AssetPack.DEFAULT_FONT_RESOURCE)
        .ensureDefaultFont();

    GameBootstrap.bootstrap(headless);
    if (!headless && getGameWindow() != null) {
      InputBridge.attachTypingListener(getGameWindow().getCanvas());
      Game.setQuitAction(
          () -> {
            BootSplash.close();
            shutdown();
            System.exit(0);
          });
    }
    // Game window is up; drop the boot splash cards.
    BootSplash.close();
  }

  @Override
  public void update(Manager manager, double delta) {
    // Transmute GameLoop delta is in frames at target FPS (1.0 = one frame), not seconds.
    // Run one Minicraft input+sim tick per engine frame when rates match (both default 60).
    tickAccumulator += delta;
    double framesPerTick = getConfig().getTargetFPS() / (double) Updater.normSpeed;
    int safety = 0;
    while (tickAccumulator >= framesPerTick && safety++ < 5) {
      InputBridge.syncFromTransmute(manager.getInputHandler());
      Updater.tick();
      tickAccumulator -= framesPerTick;
    }
  }

  @Override
  public void render(Manager manager, IRenderer renderer) {
    Renderer.renderFrame();
    Context ctx = (Context) renderer;
    int[] src = Renderer.pixelBuffer();
    int[] dst = ctx.getPixels();
    if (src == null || dst == null) {
      return;
    }
    // Transmute Context is VIEW_* * SCALE; nearest-neighbor upscale from Minicraft's buffer.
    // (Engine window size is config.size * config.scale, so we use scale(1) to avoid 9×.)
    int dw = ctx.getWidth();
    int dh = ctx.getHeight();
    for (int y = 0; y < dh; y++) {
      int sy = y / SCALE;
      if (sy >= VIEW_H) {
        break;
      }
      int srcRow = sy * VIEW_W;
      int dstRow = y * dw;
      for (int x = 0; x < dw; x++) {
        int sx = x / SCALE;
        if (sx >= VIEW_W) {
          break;
        }
        // Minicraft buffer is TYPE_INT_RGB; Context expects ARGB.
        dst[dstRow + x] = 0xFF000000 | (src[srcRow + sx] & 0x00FFFFFF);
      }
    }
  }

  public static GameConfig config(boolean headless) {
    return new GameConfig.Builder()
        .title(Game.NAME)
        .version(Game.VERSION.toString())
        // Logical framebuffer is VIEW_*×SCALE; scale(1) so the window is 3× not 9×
        // (Transmute applies size*scale for the window and size/scale for Context).
        .size(VIEW_W * SCALE, VIEW_H * SCALE)
        .scale(1)
        .headless(headless)
        .showStartScreen(false)
        .build();
  }

  public static void main(String[] args) {
    boolean headless = false;
    for (String arg : args) {
      if ("--headless".equals(arg)) {
        headless = true;
      }
    }

    if (headless) {
      try (GameHarness harness = GameHarness.of(() -> new MinicraftGame(config(true)))) {
        harness.step(2);
        // Title screen should paint non-black pixels somewhere in the buffer.
        Context ctx = (Context) harness.renderer();
        int[] px = ctx.getPixels();
        boolean anyLit = false;
        for (int p : px) {
          if ((p & 0x00FFFFFF) != 0) {
            anyLit = true;
            break;
          }
        }
        if (!anyLit) {
          throw new AssertionError("Headless smoke: expected title screen pixels");
        }
        FrameAssert.assertPixel(harness.renderer(), 0, 0, px[0]);
        System.out.println("verifyHeadless OK");
      }
      return;
    }

    BootSplash.open();
    MinicraftGame game = new MinicraftGame(config(false));
    game.start();
    // BootSplash.close() runs from init() once the game window is ready.
  }
}
