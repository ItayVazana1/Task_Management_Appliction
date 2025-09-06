package taskmanagement.ui.views;

import taskmanagement.ui.api.TasksViewAPI;
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
 *
 * <p>This class wires the UI subtree to a {@link TasksViewAPI}
 * provided from {@link taskmanagement.ui.MainFrame}.</p>
 */
public final class ContentArea extends JPanel {

    public static final int LEFT_PANEL_WIDTH  = 150;
    public static final int RIGHT_PANEL_WIDTH = 260;
    public static final int H_GUTTER          = 12;
    public static final int CENTER_MIN_WIDTH  = 650;

    private final ControlPanel leftPanel;
    private final TasksPanel tasksPanel;
    private final ToolBox rightPanel;

    private TasksViewAPI api;

    /**
     * Creates a ContentArea with default column components.
     */
    public ContentArea() {
        this(new ControlPanel(), new TasksPanel(), new ToolBox());
    }

    /**
     * Creates a ContentArea with custom column components.
     */
    public ContentArea(ControlPanel leftPanel, TasksPanel tasksPanel, ToolBox rightPanel) {
        super(new GridBagLayout());
        this.leftPanel  = (leftPanel  != null) ? leftPanel  : new ControlPanel();
        this.tasksPanel = (tasksPanel != null) ? tasksPanel : new TasksPanel();
        this.rightPanel = (rightPanel != null) ? rightPanel : new ToolBox();
        initLayout();
    }

    /**
     * Injects the {@link TasksViewAPI} into this ContentArea and its child panels.
     *
     * @param api the API to use (must not be null)
     */
    public void setApi(TasksViewAPI api) {
        this.api = api;

        // ControlPanel needs both the API and the TasksPanel (for selection).
        this.leftPanel.setApi(api, this.tasksPanel);

        // TasksPanel/ToolBox receive the API as usual.
        // (נדרש ש־TasksPanel ו־ToolBox יחשפו setApi(TasksViewAPI))
        this.tasksPanel.setApi(api);
        this.rightPanel.setApi(api);
    }

    // ---------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------

    private void initLayout() {
        setOpaque(false);

        lockWidth(leftPanel, LEFT_PANEL_WIDTH);
        lockWidth(rightPanel, RIGHT_PANEL_WIDTH);

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
