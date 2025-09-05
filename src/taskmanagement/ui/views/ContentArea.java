package taskmanagement.ui.views;

import taskmanagement.ui.widgets.ControlPanel;
import taskmanagement.ui.widgets.TasksPanel;
import taskmanagement.ui.widgets.ToolBox;

import javax.swing.*;
import java.awt.*;

/**
 * ContentArea
 * -----------
 * Three-column container: LEFT | CENTER | RIGHT.
 * LEFT and RIGHT have fixed widths; CENTER flexes to occupy remaining space.
 * The panel itself does not depend on any theme classes (pure Swing).
 *
 * Layout rules:
 * - LEFT, RIGHT: weightx=0 (fixed width via preferred/minimum size).
 * - CENTER:      weightx=1 (flex).
 * - All columns: weighty=1 and fill=BOTH to share vertical growth.
 */
public final class ContentArea extends JPanel {

    /** Fixed width (px) for the left column container. */
    public static final int LEFT_PANEL_WIDTH  = 150;
    /** Fixed width (px) for the right column container. */
    public static final int RIGHT_PANEL_WIDTH = 260;
    /** Horizontal gutter (px) to the right of LEFT and CENTER columns. */
    public static final int H_GUTTER          = 12;
    /** Optional minimum width (px) for the center column to avoid collapse. */
    public static final int CENTER_MIN_WIDTH  = 650;

    private final JComponent leftPanel;
    private final TasksPanel tasksPanel;
    private final JComponent rightPanel;

    /**
     * Creates a ContentArea with default column components:
     * left = {@link ControlPanel}, center = {@link TasksPanel}, right = {@link ToolBox}.
     */
    public ContentArea() {
        this(new ControlPanel(), new TasksPanel(), new ToolBox());
    }

    /**
     * Creates a ContentArea with custom column components.
     *
     * @param leftPanel  component for the left column (fallback: ControlPanel)
     * @param tasksPanel tasks list panel for the center column (fallback: new TasksPanel)
     * @param rightPanel component for the right column (fallback: ToolBox)
     */
    public ContentArea(JComponent leftPanel, TasksPanel tasksPanel, JComponent rightPanel) {
        super(new GridBagLayout());
        this.leftPanel  = (leftPanel  != null) ? leftPanel  : new ControlPanel();
        this.tasksPanel = (tasksPanel != null) ? tasksPanel : new TasksPanel();
        this.rightPanel = (rightPanel != null) ? rightPanel : new ToolBox();
        initLayout();
    }

    // ---------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------

    private void initLayout() {
        setOpaque(false);

        lockWidth(leftPanel, LEFT_PANEL_WIDTH);
        lockWidth(rightPanel, RIGHT_PANEL_WIDTH);

        // Equal gutters to the right of LEFT and CENTER columns
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, H_GUTTER));
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, H_GUTTER));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;

        // LEFT (fixed)
        gc.gridx = 0;
        gc.weightx = 0.0;
        add(wrap(leftPanel), gc);

        // CENTER (flex)
        gc.gridx = 1;
        gc.weightx = 1.0;
        JComponent centerWrap = wrap(tasksPanel);
        centerWrap.setMinimumSize(new Dimension(CENTER_MIN_WIDTH, 10));
        add(centerWrap, gc);

        // RIGHT (fixed)
        gc.gridx = 2;
        gc.weightx = 0.0;
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
