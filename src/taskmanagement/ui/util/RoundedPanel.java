package taskmanagement.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * A simple JPanel with rounded corners and a custom background color.
 * Opaque painting is handled manually to support transparency around corners.
 */
public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color bg;

    public RoundedPanel(Color bg, int arc) {
        super();
        this.bg = bg;
        this.arc = arc;
        setOpaque(false); // let us paint rounded shape without filling full rect
    }

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
