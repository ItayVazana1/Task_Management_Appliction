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
 * Utility methods for borderless windows with rounded corners and drag handling.
 * <p>
 * Provides helpers to make a frame undecorated with rounded corners and to
 * install a drag handler so a component can move the window.
 */
public final class WindowChrome {

    private WindowChrome() {}

    /**
     * Makes the frame borderless and applies rounded corners.
     * <p>
     * Sets the frame to undecorated, applies a transparent background, and
     * keeps a rounded shape synchronized with size changes using
     * {@link AppTheme#WINDOW_CORNER_ARC}.
     *
     * @param frame the frame to modify; no action if {@code null}
     */
    public static void makeBorderlessWithRoundedCorners(JFrame frame) {
        if (frame == null) return;

        try {
            frame.setUndecorated(true);
        } catch (IllegalComponentStateException ignore) {
            // If already visible/packed as decorated, caller should re-create before showing.
        }

        frame.setBackground(new Color(0, 0, 0, 0));
        applyRoundedShape(frame);

        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { applyRoundedShape(frame); }
            @Override public void componentShown(ComponentEvent e)   { applyRoundedShape(frame); }
        });
    }

    /**
     * Installs a drag handler so dragging the given component moves the frame.
     * Intended for use with borderless windows.
     *
     * @param frame      the frame to move; no action if {@code null}
     * @param dragHandle the component acting as a drag handle; no action if {@code null}
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

    /** Applies a rounded rectangle shape using {@link AppTheme#WINDOW_CORNER_ARC}. */
    private static void applyRoundedShape(JFrame frame) {
        int arc = AppTheme.WINDOW_CORNER_ARC;
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (w <= 0 || h <= 0) return;

        Shape round = new RoundRectangle2D.Double(0, 0, w, h, arc, arc);
        try {
            frame.setShape(round);
        } catch (UnsupportedOperationException ignored) {
            // On some platforms/VMs shapes are unsupported; ignore gracefully.
        }
    }
}
