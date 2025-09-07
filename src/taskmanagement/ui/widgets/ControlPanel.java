package taskmanagement.ui.widgets;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.ui.api.TasksViewAPI;
import taskmanagement.ui.dialogs.TaskEditorDialog;
import taskmanagement.ui.styles.AppTheme;
import taskmanagement.ui.util.RoundedPanel;
import taskmanagement.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Left-side control rail presenting five large square actions: Refresh, Add, Edit, Delete, and Disk Cleanup.
 * <p>
 * Icons are loaded safely with a transparent fallback. Width is fixed to prevent the center content
 * from shrinking the rail. Behavior delegates to a {@link TasksViewAPI} instance injected via
 * {@link #setApi(TasksViewAPI, TasksPanel)}.
 * </p>
 */
public final class ControlPanel extends RoundedPanel {

    private static final int INNER_PAD = 8;
    private static final int FALLBACK_WIDTH = 220;

    private final JButton refreshBtn;
    private final JButton addBtn;
    private final JButton editBtn;
    private final JButton deleteBtn;
    private final JButton cleanupBtn;

    private TasksViewAPI api;
    private TasksPanel tasksPanel;

    /**
     * Constructs the control rail with five vertically stacked buttons.
     */
    public ControlPanel() {
        super(new Color(0x2C2C2C), 16);
        setOpaque(false);

        int block = AppTheme.CTRL_BLOCK_SIZE;
        int fixedWidth = Math.max(FALLBACK_WIDTH, block + INNER_PAD * 2);
        Dimension fixed = new Dimension(fixedWidth, 10);
        setPreferredSize(fixed);
        setMinimumSize(fixed);

        setBorder(BorderFactory.createEmptyBorder(INNER_PAD, INNER_PAD, INNER_PAD, INNER_PAD));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        Icon refreshIcon = safeIcon(UiUtils.loadRasterIcon("/taskmanagement/ui/resources/refresh.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon addIcon = safeIcon(UiUtils.loadRasterIcon("/taskmanagement/ui/resources/add.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon editIcon = safeIcon(UiUtils.loadRasterIcon("/taskmanagement/ui/resources/edit.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon delIcon = safeIcon(UiUtils.loadRasterIcon("/taskmanagement/ui/resources/delete.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));
        Icon cleanupIcon = safeIcon(UiUtils.loadRasterIcon("/taskmanagement/ui/resources/cleanup.png",
                AppTheme.CTRL_ICON_SIZE, AppTheme.CTRL_ICON_SIZE));

        refreshBtn = UiUtils.createPaintedRoundedIconButton("Refresh", refreshIcon,
                AppTheme.CTRL_REFRESH_BG, AppTheme.CTRL_REFRESH_FG,
                block, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE);
        gbc.gridy = 0; gbc.weighty = 1.0; add(refreshBtn, gbc);

        addBtn = UiUtils.createPaintedRoundedIconButton("Add", addIcon,
                AppTheme.CTRL_ADD_BG, AppTheme.CTRL_ADD_FG,
                block, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE);
        gbc.gridy = 1; add(addBtn, gbc);

        editBtn = UiUtils.createPaintedRoundedIconButton("Edit", editIcon,
                AppTheme.CTRL_EDIT_BG, AppTheme.CTRL_EDIT_FG,
                block, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE);
        gbc.gridy = 2; add(editBtn, gbc);

        deleteBtn = UiUtils.createPaintedRoundedIconButton("Delete", delIcon,
                AppTheme.CTRL_DELETE_BG, AppTheme.CTRL_DELETE_FG,
                block, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE);
        gbc.gridy = 3; add(deleteBtn, gbc);

        cleanupBtn = UiUtils.createPaintedRoundedIconButton("Cleanup", cleanupIcon,
                AppTheme.CTRL_CLEANUP_BG, AppTheme.CTRL_CLEANUP_FG,
                block, AppTheme.CTRL_CORNER_RAD, AppTheme.CTRL_FONT_SIZE);
        gbc.gridy = 4; add(cleanupBtn, gbc);

        wireActions();
    }

    /**
     * Injects the API and the tasks panel used for selection-dependent operations.
     *
     * @param api        the {@link TasksViewAPI} implementation
     * @param tasksPanel the tasks panel providing selection information
     * @throws NullPointerException if any argument is {@code null}
     */
    public void setApi(TasksViewAPI api, TasksPanel tasksPanel) {
        this.api = Objects.requireNonNull(api, "api");
        this.tasksPanel = Objects.requireNonNull(tasksPanel, "tasksPanel");
    }

    private void wireActions() {
        refreshBtn.addActionListener(this::onRefresh);
        addBtn.addActionListener(this::onAdd);
        editBtn.addActionListener(this::onEdit);
        deleteBtn.addActionListener(this::onDelete);
        cleanupBtn.addActionListener(this::onCleanupAll);
    }

    private void onRefresh(ActionEvent e) {
        if (api != null) {
            api.reload();
        }
    }

    private void onAdd(ActionEvent e) {
        Optional<TaskEditorDialog.EditorResult> result =
                TaskEditorDialog.showDialog(this, TaskEditorDialog.Mode.ADD, null);
        result.ifPresent(r -> {
            if (api != null) {
                api.addTask(new ITask() {
                    @Override public int getId() { return 0; }
                    @Override public String getTitle() { return r.title(); }
                    @Override public String getDescription() { return r.description(); }
                    @Override public TaskState getState() { return r.state(); }
                    @Override public void accept(taskmanagement.domain.visitor.TaskVisitor v) {}
                });
            }
        });
    }

    private void onEdit(ActionEvent e) {
        if (tasksPanel == null || api == null) {
            return;
        }
        List<Integer> ids = tasksPanel.getSelectedIds();
        if (ids.size() != 1) {
            JOptionPane.showMessageDialog(this,
                    "Please select exactly one task to edit.",
                    "Edit Task",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = ids.get(0);

        api.findRowById(id).ifPresent(row -> {
            TaskEditorDialog.Prefill prefill = new TaskEditorDialog.Prefill(
                    row.id(),
                    row.title(),
                    row.description(),
                    TaskState.valueOf(row.state())
            );

            Optional<TaskEditorDialog.EditorResult> result =
                    TaskEditorDialog.showDialog(this, TaskEditorDialog.Mode.EDIT, prefill);

            result.ifPresent(r -> {
                api.updateTask(new ITask() {
                    @Override public int getId() { return r.id(); }
                    @Override public String getTitle() { return r.title(); }
                    @Override public String getDescription() { return r.description(); }
                    @Override public TaskState getState() { return r.state(); }
                    @Override public void accept(taskmanagement.domain.visitor.TaskVisitor v) {}
                });
                api.reload();
            });
        });
    }

    private void onDelete(ActionEvent e) {
        if (api != null && tasksPanel != null && showConfirmDeleteSelected()) {
            List<Integer> ids = tasksPanel.getSelectedIds();
            if (!ids.isEmpty()) {
                api.deleteTasks(ids.stream().mapToInt(Integer::intValue).toArray());
            }
        }
    }

    private void onCleanupAll(ActionEvent e) {
        if (api != null && showConfirmDeleteAll()) {
            api.deleteAll();
        }
    }

    private boolean showConfirmDeleteSelected() {
        int ans = JOptionPane.showConfirmDialog(
                this,
                "Delete selected task(s)?",
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return ans == JOptionPane.OK_OPTION;
    }

    private boolean showConfirmDeleteAll() {
        int ans = JOptionPane.showConfirmDialog(
                this,
                "Delete ALL tasks? This cannot be undone.",
                "Disk Cleanup",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return ans == JOptionPane.YES_OPTION;
    }

    private static Icon safeIcon(Icon icon) {
        if (icon != null) return icon;
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
            @Override public int getIconWidth() { return 1; }
            @Override public int getIconHeight() { return 1; }
        };
    }
}
