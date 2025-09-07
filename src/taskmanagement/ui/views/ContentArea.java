package taskmanagement.ui.views;

import taskmanagement.ui.api.TasksViewAPI;
import taskmanagement.ui.widgets.ControlPanel;
import taskmanagement.ui.widgets.TasksPanel;
import taskmanagement.ui.widgets.ToolBox;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Three-column container composed of LEFT, CENTER, and RIGHT panes.
 * LEFT and RIGHT have fixed widths while CENTER flexes to occupy remaining space.
 * <p>
 * This view wires its child widgets to a {@link TasksViewAPI} instance and bridges
 * the current selection from {@link TasksPanel} into {@link ToolBox} so actions
 * (e.g., Advance/Mark-as) operate on the selected task IDs.
 * </p>
 */
public final class ContentArea extends JPanel {

    /** Fixed width in pixels for the left rail. */
    public static final int LEFT_PANEL_WIDTH = 150;
    /** Fixed width in pixels for the right rail. */
    public static final int RIGHT_PANEL_WIDTH = 260;
    /** Horizontal gutter between columns, in pixels. */
    public static final int H_GUTTER = 12;
    /** Minimal width for the center content, in pixels. */
    public static final int CENTER_MIN_WIDTH = 650;

    private final ControlPanel leftPanel;
    private final TasksPanel tasksPanel;
    private final ToolBox rightPanel;

    private TasksViewAPI api;

    /**
     * Creates a {@code ContentArea} with default column components.
     */
    public ContentArea() {
        this(new ControlPanel(), new TasksPanel(), new ToolBox());
    }

    /**
     * Creates a {@code ContentArea} with custom column components.
     *
     * @param leftPanel  the left rail widget; if {@code null}, a default instance is used
     * @param tasksPanel the center tasks widget; if {@code null}, a default instance is used
     * @param rightPanel the right rail widget; if {@code null}, a default instance is used
     */
    public ContentArea(ControlPanel leftPanel, TasksPanel tasksPanel, ToolBox rightPanel) {
        super(new GridBagLayout());
        this.leftPanel = (leftPanel != null) ? leftPanel : new ControlPanel();
        this.tasksPanel = (tasksPanel != null) ? tasksPanel : new TasksPanel();
        this.rightPanel = (rightPanel != null) ? rightPanel : new ToolBox();
        initLayout();
    }

    /**
     * Injects the {@link TasksViewAPI} and wires child widgets and interactions.
     * The right panel receives a provider for the currently selected task IDs.
     *
     * @param api the UI-facing ViewModel API
     * @throws NullPointerException if {@code api} is {@code null}
     */
    public void setApi(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");
        this.leftPanel.setApi(this.api, this.tasksPanel);
        this.tasksPanel.setApi(this.api);
        this.rightPanel.setApi(this.api);
        this.rightPanel.setIdsProvider(tasksPanel::selectedIds);
        this.rightPanel.bindSelectionProperty(tasksPanel.selectedIdsProperty());
        this.rightPanel.bindTotalsFromApi();
        this.rightPanel.bindAdvanceAndMarkDialogs();
        this.rightPanel.bindSortControls(List.of(
                new taskmanagement.application.viewmodel.sort.SortById(),
                new taskmanagement.application.viewmodel.sort.SortByTitle(),
                new taskmanagement.application.viewmodel.sort.SortByState()
        ));
        this.rightPanel.bindFilterControls(this.api);
        this.api.reload();
    }

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
        gc.gridx = 0;
        gc.weightx = 0.0;
        add(wrap(leftPanel), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        JComponent centerWrap = wrap(tasksPanel);
        centerWrap.setMinimumSize(new Dimension(CENTER_MIN_WIDTH, 10));
        add(centerWrap, gc);
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
        comp.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
    }
}
