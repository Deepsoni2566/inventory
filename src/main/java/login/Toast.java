package login;

import javax.swing.*;
import java.awt.*;

/**
 * Simple non-modal toast popup.
 */
public class Toast {
    /**
     * Show a brief message in the top-right, or fixed position if owner isn’t visible.
     * @param owner parent JFrame (may not yet be visible)
     * @param msg   message to display
     */
    public static void show(JFrame owner, String msg) {
        JWindow w = new JWindow(owner);
        w.setLayout(new BorderLayout());
        w.add(new JLabel(msg, SwingConstants.CENTER), BorderLayout.CENTER);
        w.setSize(250, 60);

        // position: if owner is on screen, top-right of owner; otherwise fixed on screen
        Point loc;
        if (owner.isShowing()) {
            loc = owner.getLocationOnScreen();
            w.setLocation(loc.x + owner.getWidth() - 260, loc.y + 10);
        } else {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            w.setLocation(screen.width - 300, 50);
        }

        w.setVisible(true);
        // hide after 3s
        new javax.swing.Timer(3000, e -> {
            w.setVisible(false);
            w.dispose();
        }).start();
    }
}
