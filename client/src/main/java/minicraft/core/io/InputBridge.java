package minicraft.core.io;

import TransmuteCore.core.interfaces.services.IInputHandler;
import minicraft.core.Game;

import java.awt.event.KeyEvent;

/**
 * Copies Transmute key state into Minicraft {@link InputHandler} each frame.
 */
public final class InputBridge
{
    private static final int[] TRACKED = {
        KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
        KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
        KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE, KeyEvent.VK_SPACE,
        KeyEvent.VK_C, KeyEvent.VK_X, KeyEvent.VK_E, KeyEvent.VK_Z,
        KeyEvent.VK_V, KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R,
        KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT,
        KeyEvent.VK_F3, KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN
    };

    private InputBridge()
    {
    }

    public static void syncFromTransmute(IInputHandler handler)
    {
        if (handler == null || Game.input == null)
        {
            return;
        }

        for (int code : TRACKED)
        {
            boolean down = handler.isKeyHeld(code) || handler.isKeyPressed(code);
            Game.input.injectKey(code, down);
        }
        Game.input.tick();
    }
}
