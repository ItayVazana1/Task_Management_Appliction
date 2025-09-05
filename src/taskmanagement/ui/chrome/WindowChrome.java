package taskmanagement.ui.chrome;

import taskmanagement.ui.styles.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * WindowChrome
 * Utilities for borderless windows with rounded corners and drag handling.
 *
 * Features:
 * - makeBorderlessWithRoundedCorners(JFrame): sets undecorated, transparent background,
 *   and keeps a rounded shape synced to window size using AppTheme.WINDOW_CORNER_ARC.
 * - installDragHandler(JFrame, JComponent): makes the given component act as a drag handle.
 *
 * Notes:
 * - This class is UI-agnostic; it does not impose a Look&Feel.
 * - Comments in English only.
 */
public final class WindowChrome {

    private WindowChrome() {}

    /**
     * Make the frame borderless and apply rounded corners.
     * Safe to call once after frame content is set (before showing).
     */
    public static void makeBorderlessWithRoundedCorners(JFrame frame) {
        if (frame == null) return;

        // Undecorated borderless window
        try {
            frame.setUndecorated(true);
        } catch (IllegalComponentStateException ignore) {
            // If already visible/packed as decorated, caller should re-create before showing.
        }

        // Ensure background supports translucency (for smooth corners on some platforms)
        frame.setBackground(new Color(0, 0, 0, 0));

        // Apply initial shape
        applyRoundedShape(frame);

        // Keep shape in sync with window size changes
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { applyRoundedShape(frame); }
            @Override public void componentShown(ComponentEvent e)   { applyRoundedShape(frame); }
        });
    }

    /**
     * Install a drag handler so dragging the given handle moves the frame.
     * This is useful when the window is borderless.
     */
    public static void installDragHandler(JFrame frame, JComponent dragHandle) {
        if (frame == null || dragHandle == null) return;

        final Point[] origin = new Point[1];

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                origin[0] = e.getPoint();
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (origin[0] != null) {
                    Point p = e.getLocationOnScreen();
                    Insets ins = frame.getInsets();
                    frame.setLocation(p.x - origin[0].x - ins.left, p.y - origin[0].y - ins.top);
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                origin[0] = null;
            }
        };

        dragHandle.addMouseListener(ma);
        dragHandle.addMouseMotionListener(ma);
    }

    // ---- internal ----

    /** Apply a rounded rectangle shape using AppTheme.WINDOW_CORNER_ARC. */
    private static void applyRoundedShape(JFrame frame) {
        int arc = AppTheme.WINDOW_CORNER_ARC;
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (w <= 0 || h <= 0) return;

        // For Java 9+, setShape works with transparent backgrounds.
        Shape round = new RoundRectangle2D.Double(0, 0, w, h, arc, arc);
        try {
            frame.setShape(round);
        } catch (UnsupportedOperationException ignored) {
            // On some platforms/VMs shapes are unsupported; ignore gracefully.
        }
    }
}
