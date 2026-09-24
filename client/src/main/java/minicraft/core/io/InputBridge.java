package minicraft.core.io;

import TransmuteCore.core.interfaces.services.IInputHandler;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import minicraft.core.Game;

/** Copies Transmute key state into Minicraft {@link InputHandler} each frame. */
public final class InputBridge {
  private static final int[] TRACKED = {
    KeyEvent.VK_UP,
    KeyEvent.VK_DOWN,
    KeyEvent.VK_LEFT,
    KeyEvent.VK_RIGHT,
    KeyEvent.VK_W,
    KeyEvent.VK_A,
    KeyEvent.VK_S,
    KeyEvent.VK_D,
    KeyEvent.VK_ENTER,
    KeyEvent.VK_ESCAPE,
    KeyEvent.VK_SPACE,
    KeyEvent.VK_C,
    KeyEvent.VK_X,
    KeyEvent.VK_E,
    KeyEvent.VK_Z,
    KeyEvent.VK_V,
    KeyEvent.VK_P,
    KeyEvent.VK_Q,
    KeyEvent.VK_R,
    KeyEvent.VK_F,
    KeyEvent.VK_O,
    KeyEvent.VK_L,
    KeyEvent.VK_I,
    KeyEvent.VK_SHIFT,
    KeyEvent.VK_CONTROL,
    KeyEvent.VK_ALT,
    KeyEvent.VK_F1,
    KeyEvent.VK_F2,
    KeyEvent.VK_F3,
    KeyEvent.VK_F11,
    KeyEvent.VK_PAGE_UP,
    KeyEvent.VK_PAGE_DOWN,
    KeyEvent.VK_BACK_SPACE,
    KeyEvent.VK_DELETE
  };

  private static boolean typingAttached;

  private InputBridge() {}

  /**
   * Forward AWT {@code keyTyped} from the Transmute canvas into Minicraft. Transmute's Input
   * ignores typed chars, so world-name / text fields would otherwise never receive input.
   */
  public static void attachTypingListener(Component canvas) {
    if (canvas == null || typingAttached) {
      return;
    }
    typingAttached = true;
    canvas.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyTyped(KeyEvent e) {
            if (Game.input != null) {
              Game.input.keyTyped(e);
            }
          }
        });
  }

  public static void syncFromTransmute(IInputHandler handler) {
    if (handler == null || Game.input == null) {
      return;
    }

    for (int code : TRACKED) {
      boolean down = handler.isKeyHeld(code) || handler.isKeyPressed(code);
      Game.input.injectKey(code, down);
    }
    Game.input.tick();
  }
}
