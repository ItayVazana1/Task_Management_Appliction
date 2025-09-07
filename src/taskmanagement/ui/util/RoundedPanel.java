package taskmanagement.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * A custom {@link JPanel} with rounded corners and a configurable background color.
 * <p>
 * This panel disables default opacity so that its rounded edges
 * can blend seamlessly with the parent background.
 * </p>
 */
public class RoundedPanel extends JPanel {

    /** Radius (in pixels) used for drawing rounded corners. */
    private final int arc;

    /** Background color used to fill the rounded panel. */
    private final Color bg;

    /**
     * Creates a new rounded panel with the specified background color and corner radius.
     *
     * @param bg  the background color of the panel
     * @param arc the radius (in pixels) for the rounded corners
     */
    public RoundedPanel(Color bg, int arc) {
        super();
        this.bg = bg;
        this.arc = arc;
        setOpaque(false); // ensure background outside rounded area is transparent
    }

    /**
     * Paints the panel with rounded corners using the configured background color.
     *
     * @param g the {@link Graphics} context used for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
