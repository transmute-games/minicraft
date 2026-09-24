package minicraft.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import minicraft.util.Logging;

/**
 * Shows the bundled Minicraft+ boot splash frames (green logo cards) while the game starts.
 * Original assets lived unused after the Transmute window took over startup.
 */
public final class BootSplash {
  private static final String[] RESOURCE_PATHS = {
    "/Minicraft_Splash_Screen_1.png",
    "/Minicraft_Splash_Screen_2.png",
    "/Minicraft_Splash_Screen_3.png"
  };

  private static JWindow window;
  private static Timer animator;
  private static JLabel label;
  private static List<ImageIcon> frames = List.of();
  private static int frameIndex;

  private BootSplash() {}

  /** Open the splash on the EDT; no-op if assets are missing or already open. */
  public static void open() {
    if (window != null) {
      return;
    }
    List<ImageIcon> loaded = loadFrames();
    if (loaded.isEmpty()) {
      Logging.GAMEHANDLER.warn("Boot splash images missing; skipping splash.");
      return;
    }
    frames = loaded;
    frameIndex = 0;

    try {
      SwingUtilities.invokeAndWait(
          () -> {
            label = new JLabel(frames.get(0));
            label.setOpaque(true);
            label.setBackground(Color.BLACK);

            window = new JWindow();
            window.setLayout(new BorderLayout());
            window.add(label, BorderLayout.CENTER);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setAlwaysOnTop(true);
            window.setVisible(true);

            animator =
                new Timer(
                    450,
                    e -> {
                      if (window == null || frames.isEmpty()) {
                        return;
                      }
                      frameIndex = (frameIndex + 1) % frames.size();
                      label.setIcon(frames.get(frameIndex));
                    });
            animator.start();
          });
    } catch (Exception e) {
      Logging.GAMEHANDLER.warn(e, "Failed to show boot splash.");
      close();
    }
  }

  /** Hide and dispose the splash (safe to call multiple times). */
  public static void close() {
    Runnable dispose =
        () -> {
          if (animator != null) {
            animator.stop();
            animator = null;
          }
          if (window != null) {
            window.setVisible(false);
            window.dispose();
            window = null;
          }
          label = null;
          frames = List.of();
        };
    if (SwingUtilities.isEventDispatchThread()) {
      dispose.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(dispose);
      } catch (Exception ignored) {
        dispose.run();
      }
    }
  }

  private static List<ImageIcon> loadFrames() {
    ArrayList<ImageIcon> icons = new ArrayList<>();
    for (String path : RESOURCE_PATHS) {
      try (InputStream in = BootSplash.class.getResourceAsStream(path)) {
        if (in == null) {
          continue;
        }
        Image image = ImageIO.read(in);
        if (image != null) {
          icons.add(new ImageIcon(image));
        }
      } catch (IOException e) {
        Logging.GAMEHANDLER.warn(e, "Could not load splash frame {}", path);
      }
    }
    return icons;
  }
}
