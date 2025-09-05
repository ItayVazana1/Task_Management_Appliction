package ui_test.views;

import ui_test.widgets.ControlPanel;
import ui_test.widgets.TasksPanel;
import ui_test.widgets.ToolBox;

import javax.swing.*;
import java.awt.*;

/**
 * ContentArea lays out three columns: LEFT | CENTER | RIGHT.
 * - LEFT and RIGHT: fixed widths (preferred == minimum), weightx = 0.
 * - CENTER: flexible (takes remaining width), weightx = 1.
 * - Vertical growth is shared by all via weighty = 1 and fill = BOTH.
 *
 * This class does not depend on external theme classes. Widths can be tuned
 * by changing the constants below.
 */
public final class ContentArea extends JPanel {

    /** Fixed widths (px) for side panels and horizontal gutter between columns. */
    public static final int LEFT_PANEL_WIDTH  = 150;
    public static final int RIGHT_PANEL_WIDTH = 260;
    public static final int H_GUTTER          = 12;
    /** Optional minimum width for center area to avoid extreme collapse. */
    public static final int CENTER_MIN_WIDTH  = 650;


    private final JComponent leftPanel;
    private final TasksPanel tasksPanel;
    private final JComponent rightPanel;

    public ContentArea() {
        this(new ControlPanel(), new TasksPanel(), new ToolBox());
    }

    public ContentArea(JComponent leftPanel, TasksPanel tasksPanel, JComponent rightPanel) {
        super(new GridBagLayout());
        this.leftPanel  = (leftPanel  != null) ? leftPanel  : new ControlPanel();
        this.tasksPanel = (tasksPanel != null) ? tasksPanel : new TasksPanel();
        this.rightPanel = (rightPanel != null) ? rightPanel : new ToolBox();
        initLayout();
    }

    private void initLayout() {
        setOpaque(false);

        lockWidth(leftPanel, LEFT_PANEL_WIDTH);
        lockWidth(rightPanel, RIGHT_PANEL_WIDTH);

        // Equal gutters on both sides of the center list
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, H_GUTTER));
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, H_GUTTER));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;

        // LEFT (fixed)
        gc.gridx = 0; gc.weightx = 0.0;
        add(wrap(leftPanel), gc);

        // CENTER (flex)
        gc.gridx = 1; gc.weightx = 1.0;
        JComponent centerWrap = wrap(tasksPanel);
        centerWrap.setMinimumSize(new Dimension(CENTER_MIN_WIDTH, 10));
        add(centerWrap, gc);

        // RIGHT (fixed)
        gc.gridx = 2; gc.weightx = 0.0;
        add(wrap(rightPanel), gc);
    }

    private static JComponent wrap(JComponent inner) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private static void lockWidth(JComponent comp, int width) {
        Dimension pref = comp.getPreferredSize();
        if (pref == null) pref = new Dimension(width, 10);
        Dimension fixed = new Dimension(width, Math.max(10, pref.height));
        comp.setPreferredSize(fixed);
        comp.setMinimumSize(fixed);
    }
}