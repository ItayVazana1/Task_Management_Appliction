package taskmanagement.ui.views;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;
import taskmanagement.ui.views.panels.FiltersPanel;
import taskmanagement.ui.views.panels.TaskEditorDialog;
import taskmanagement.ui.views.panels.TaskListPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Main application window (pure Java, no .form).
 * Bridges UI actions to {@link TasksViewModel} and applies client-side filter/sort.
 */
public final class MainFrame extends JFrame {

    private final TasksViewModel vm;

    // UI
    private final FiltersPanel filters;
    private final TaskListPanel list;

    // Client-side filtering & sorting (UI-level)
    private String titleContains = "";
    private TaskState stateEquals = null;
    private String sortKey = "id"; // "id" | "title" | "state"

    // Keep what VM returns: RowDTO (not ITask!)
    private List<TasksViewModel.RowDTO> masterRows = new ArrayList<>();

    /**
     * Constructs the main window. Pass your concrete {@link TasksViewModel}.
     */
    public MainFrame(TasksViewModel viewModel) {
        super("Tasks Management");
        this.vm = Objects.requireNonNull(viewModel, "viewModel");

        UITheme.applyGlobalDefaults();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setLocationByPlatform(true);
        setLayout(new BorderLayout());

        // Panels
        this.filters = new FiltersPanel(this::onApplyFilter, this::onSortChanged);
        this.list = new TaskListPanel(this::onAdd, this::onEdit, this::onDelete, this::onMarkState);

        setJMenuBar(buildMenuBar());
        add(filters.getComponent(), BorderLayout.NORTH);
        add(list.getComponent(), BorderLayout.CENTER);

        // First load
        reloadFromVM();

        pack();
    }

    // ===== Menu =====

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> safeRun(this::reloadFromVM));
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());
        file.add(refresh);
        file.addSeparator();
        file.add(exit);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> new AboutDialog(this).setVisible(true));
        help.add(about);

        mb.add(file);
        mb.add(help);
        return mb;
    }

    // ===== Actions from FiltersPanel =====

    private void onApplyFilter(String titleContains, TaskState stateOrNull) {
        this.titleContains = titleContains == null ? "" : titleContains.trim();
        this.stateEquals = stateOrNull;
        applyFilterAndSort();
    }

    private void onSortChanged(String key) {
        if (key != null) this.sortKey = key;
        applyFilterAndSort();
    }

    // ===== Actions from TaskListPanel =====
    // NOTICE: no "throws" here; all checked exceptions are handled inside.

    private void onAdd() {
        TaskEditorDialog dlg = new TaskEditorDialog(this, "Add Task", null);
        var r = dlg.showDialog();
        if (r != null) {
            safeRun(() -> {
                try {
                    vm.addTask(r.title(), r.description(), r.state());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                reloadFromVM();
            });
        }
    }

    private void onEdit(int id) {
        TasksViewModel.RowDTO existing = findById(id);
        if (existing == null) return;
        TaskEditorDialog dlg = new TaskEditorDialog(this, "Edit Task", new ViewTask(existing));
        var r = dlg.showDialog();
        if (r != null) {
            safeRun(() -> {
                try {
                    vm.updateTask(id, r.title(), r.description(), r.state());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                reloadFromVM();
            });
        }
    }

    private void onDelete(int id) {
        int ans = JOptionPane.showConfirmDialog(
                this, "Delete task #" + id + "?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            safeRun(() -> {
                vm.deleteTask(id);
                reloadFromVM();
            });
        }
    }

    private void onMarkState(int id, TaskState state) {
        safeRun(() -> {
            try {
                vm.markState(id, state);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            reloadFromVM();
        });
    }

    // ===== Data flow =====

    /** Pull fresh rows from VM and re-apply UI filtering/sorting. */
    private void reloadFromVM() {
        vm.reload();
        masterRows = new ArrayList<>(vm.getRows()); // RowDTO list
        applyFilterAndSort();
    }

    /** Apply current UI filters & sorting to masterRows and push to table. */
    private void applyFilterAndSort() {
        // Filter
        Predicate<TasksViewModel.RowDTO> p = t -> true;
        if (!titleContains.isEmpty()) {
            String needle = titleContains.toLowerCase();
            p = p.and(t -> t.title() != null && t.title().toLowerCase().contains(needle));
        }
        if (stateEquals != null) {
            // RowDTO.state() is String; compare to enum name
            p = p.and(t -> Objects.equals(t.state(), stateEquals.name()));
        }
        List<TasksViewModel.RowDTO> filtered = masterRows.stream().filter(p).toList();

        // Sort
        Comparator<TasksViewModel.RowDTO> cmp = switch (sortKey) {
            case "title" -> Comparator.comparing(TasksViewModel.RowDTO::title, String.CASE_INSENSITIVE_ORDER);
            case "state" -> Comparator.comparing(TasksViewModel.RowDTO::state); // String comparison
            default -> Comparator.comparingInt(TasksViewModel.RowDTO::id);
        };
        filtered = filtered.stream().sorted(cmp).toList();

        // Map to ITask wrapper for the table
        List<ITask> tableRows = filtered.stream()
                .map(ViewTask::new)
                .map(it -> (ITask) it)
                .collect(Collectors.toList());

        list.setRows(tableRows);
    }

    private TasksViewModel.RowDTO findById(int id) {
        for (TasksViewModel.RowDTO t : masterRows) if (t.id() == id) return t;
        return null;
    }

    // ===== Error handling helper =====

    private void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() == null ? ex.toString() : ex.getMessage(),
                    "Operation Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Thin immutable wrapper that adapts a RowDTO (id, title, description, state as String)
     * to the ITask interface required by UI components.
     */
    private static final class ViewTask implements ITask {
        private final int id;
        private final String title;
        private final String description;
        private final TaskState state;

        ViewTask(TasksViewModel.RowDTO r) {
            this.id = r.id();
            this.title = r.title();
            this.description = r.description();
            this.state = safeParseState(r.state());
        }

        private static TaskState safeParseState(String s) {
            if (s == null) return TaskState.ToDo;
            try {
                return TaskState.valueOf(s);
            } catch (IllegalArgumentException ex) {
                return switch (s.trim()) {
                    case "TODO", "To Do", "To_Do", "toDo" -> TaskState.ToDo;
                    case "IN_PROGRESS", "In Progress", "inProgress" -> TaskState.InProgress;
                    case "DONE", "Complete", "Completed" -> TaskState.Completed;
                    default -> TaskState.ToDo;
                };
            }
        }

        @Override public int getId() { return id; }
        @Override public String getTitle() { return title; }
        @Override public String getDescription() { return description; }
        @Override public TaskState getState() { return state; }

        @Override
        public void accept(taskmanagement.domain.visitor.TaskVisitor visitor) {
            // no-op in UI layer
        }
    }
}
