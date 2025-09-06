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
 * ContentArea
 * -----------
 * Three-column container: LEFT | CENTER | RIGHT.
 * LEFT and RIGHT have fixed widths; CENTER flexes to occupy remaining space.
 *
 * <p>Wires the UI subtree to a {@link TasksViewAPI} instance and bridges
 * selection from {@link TasksPanel} into {@link ToolBox} so actions like
 * Advance/Mark-as operate on the currently selected task IDs.</p>
 */
public final class ContentArea extends JPanel {

    /** Fixed width in pixels for the left rail. */
    public static final int LEFT_PANEL_WIDTH  = 150;
    /** Fixed width in pixels for the right rail. */
    public static final int RIGHT_PANEL_WIDTH = 260;
    /** Horizontal gutter between columns, in pixels. */
    public static final int H_GUTTER          = 12;
    /** Minimal width for the center content, in pixels. */
    public static final int CENTER_MIN_WIDTH  = 650;

    private final ControlPanel leftPanel;
    private final TasksPanel   tasksPanel;
    private final ToolBox      rightPanel;

    private TasksViewAPI api;

    /**
     * Creates a ContentArea with default column components.
     */
    public ContentArea() {
        this(new ControlPanel(), new TasksPanel(), new ToolBox());
    }

    /**
     * Creates a ContentArea with custom column components.
     *
     * @param leftPanel  the left rail widget (may be null to use default)
     * @param tasksPanel the center tasks widget (may be null to use default)
     * @param rightPanel the right rail widget (may be null to use default)
     */
    public ContentArea(ControlPanel leftPanel, TasksPanel tasksPanel, ToolBox rightPanel) {
        super(new GridBagLayout());
        this.leftPanel  = (leftPanel  != null) ? leftPanel  : new ControlPanel();
        this.tasksPanel = (tasksPanel != null) ? tasksPanel : new TasksPanel();
        this.rightPanel = (rightPanel != null) ? rightPanel : new ToolBox();
        initLayout();
    }

    /**
     * Injects the {@link TasksViewAPI} into this ContentArea and wires child widgets.
     *
     * <p>Bridging rules:
     * <ul>
     *   <li>Left rail (ControlPanel) gets the API and the {@link TasksPanel} for bulk actions.</li>
     *   <li>Center (TasksPanel) gets the API for row-level actions.</li>
     *   <li>Right rail (ToolBox) gets the API and a provider of selected IDs from {@link TasksPanel}.</li>
     * </ul>
     * </p>
     *
     * @param api the UI-facing ViewModel API (must not be null)
     * @throws NullPointerException if api is null
     */
    public void setApi(TasksViewAPI api) {
        this.api = Objects.requireNonNull(api, "api");

        // LEFT rail: may trigger VM operations and sometimes needs selection utilities.
        this.leftPanel.setApi(this.api, this.tasksPanel);

        // CENTER: binds to VM for row actions and list rendering.
        this.tasksPanel.setApi(this.api);

        // RIGHT rail: API + selected IDs provider + counters + dialogs.
        this.rightPanel.setApi(this.api);
        this.rightPanel.setIdsProvider(tasksPanel::selectedIds);

        // Counters (auto-updating via Observer pattern).
        this.rightPanel.bindSelectionProperty(tasksPanel.selectedIdsProperty());
        this.rightPanel.bindTotalsFromApi();

        // Replace placeholders for Advance/Mark-as with real dialogs + VM calls.
        this.rightPanel.bindAdvanceAndMarkDialogs();

        // ---- Sort wiring ----
        // Stream strategies into the ToolBox (first = default "ID").
        this.rightPanel.bindSortControls(List.of(
                new taskmanagement.application.viewmodel.sort.SortById(),     // Default
                new taskmanagement.application.viewmodel.sort.SortByTitle(),
                new taskmanagement.application.viewmodel.sort.SortByState()
        ));

        // ---- Filter wiring ----
        // Connect ToolBox Apply/Reset to the API (VM setFilter/clearFilter).
        this.rightPanel.bindFilterControls(this.api);

        // Optional: initial load so center panel shows data on first display.
        // Adapter wraps exceptions; no UI popups here.
        this.api.reload();
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
        gc.gridy   = 0;
        gc.fill    = GridBagConstraints.BOTH;
        gc.weighty = 1.0;

        // LEFT (fixed)
        gc.gridx   = 0;
        gc.weightx = 0.0;
        add(wrap(leftPanel), gc);

        // CENTER (flex)
        gc.gridx   = 1;
        gc.weightx = 1.0;
        JComponent centerWrap = wrap(tasksPanel);
        centerWrap.setMinimumSize(new Dimension(CENTER_MIN_WIDTH, 10));
        add(centerWrap, gc);

        // RIGHT (fixed)
        gc.gridx   = 2;
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
