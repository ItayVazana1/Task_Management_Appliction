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
 * Main application window (pure Swing, no .form).
 * <p>
 * Layout aligned to the mock:
 * <ul>
 *   <li>Top: lime header title.</li>
 *   <li>West: big action buttons (Add / Edit / Delete).</li>
 *   <li>Center: task list panel wrapped with JScrollPane.</li>
 *   <li>East: Filters panel (hosted inside FiltersPanel itself).</li>
 * </ul>
 * <b>Design-only changes — no VM calls were modified.</b>
 */
public final class MainFrame extends JFrame {

    private final TasksViewModel vm;

    // UI panels (provided implementations)
    private final FiltersPanel filters;
    private final TaskListPanel list;

    // Current UI filtering & sorting (client-side)
    private String titleContains = "";
    private TaskState stateEquals = null;
    private String sortKey = "id"; // "id" | "title" | "state"

    // Data snapshot from VM (RowDTO list, not ITask)
    private List<TasksViewModel.RowDTO> masterRows = new ArrayList<>();

    /**
     * Constructs the main window. Pass your concrete {@link TasksViewModel}.
     */
    public MainFrame(TasksViewModel viewModel) {
        super("Tasks Management");
        this.vm = Objects.requireNonNull(viewModel, "viewModel");

        // Global LaF and defaults (dark theme tokens)
        UITheme.applyGlobalDefaults();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 🔒 Prevent resizing (per spec)
        setResizable(false);

        // 📏 Fixed initial size (per spec)
        setPreferredSize(new Dimension(1280, 720));

        // ===== Root layout: title top, actions west, list center, filters east =====
        setLayout(new BorderLayout(16, 0));
        getContentPane().setBackground(UITheme.BG_APP);

        // Menu bar (File/Help…)
        setJMenuBar(buildMenuBar());

        // Top lime header
        add(buildHeader(), BorderLayout.NORTH);

        // West big action buttons
        JComponent leftActions = buildLeftActions();
        // ⬅️ Fixed width per spec
        leftActions.setPreferredSize(new Dimension(160, 0));
        add(leftActions, BorderLayout.WEST);

        // Center task list (wrapped by JScrollPane per spec)
        this.list = new TaskListPanel(this::onAdd, this::onEdit, this::onDelete, this::onMarkState);
        JScrollPane centerScroll = new JScrollPane(list.getComponent());
        centerScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        centerScroll.getViewport().setBackground(UITheme.BG_CARD);
        add(centerScroll, BorderLayout.CENTER);

        // East filters panel
        this.filters = new FiltersPanel(this::onApplyFilter, this::onSortChanged);
        JComponent rightFilters = buildRightFilters();
        // ➡️ Fixed width per spec (340px)
        rightFilters.setPreferredSize(new Dimension(340, 0));
        add(rightFilters, BorderLayout.EAST);

        // First load
        reloadFromVM();

        pack();
        setLocationRelativeTo(null);
    }

    // ===== Header (North) =====

    /**
     * Builds the top header with brand lime title, aligned left.
     */
    private JComponent buildHeader() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        north.setOpaque(false);
        north.add(UITheme.makeHeaderTitle("Task Management App"));
        return north;
    }

    // ===== Left action bar (West) =====

    /**
     * Builds the left vertical bar with big action buttons.
     */
    private JComponent buildLeftActions() {
        JPanel west = new JPanel();
        west.setOpaque(false);
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));

        JButton btnAdd = new JButton("Add", UITheme.whiteSquareIcon(20));
        UITheme.styleActionButton(btnAdd, UITheme.BTN_ADD_BG);
        btnAdd.addActionListener(e -> onAdd());

        JButton btnEdit = new JButton("Edit", UITheme.whiteSquareIcon(20));
        UITheme.styleActionButton(btnEdit, UITheme.BTN_EDIT_BG);
        btnEdit.addActionListener(e -> {
            Integer selectedId = tryGetSelectedId();
            if (selectedId == null) {
                JOptionPane.showMessageDialog(this, "Select a task in the list to edit.", "No Selection",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            onEdit(selectedId);
        });

        JButton btnDelete = new JButton("Delete", UITheme.whiteSquareIcon(20));
        UITheme.styleActionButton(btnDelete, UITheme.BTN_DELETE_BG);
        btnDelete.addActionListener(e -> {
            Integer selectedId = tryGetSelectedId();
            if (selectedId == null) {
                JOptionPane.showMessageDialog(this, "Select a task in the list to delete.", "No Selection",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            onDelete(selectedId);
        });

        west.add(Box.createVerticalStrut(8));
        west.add(btnAdd);
        west.add(Box.createVerticalStrut(16));
        west.add(btnEdit);
        west.add(Box.createVerticalStrut(16));
        west.add(btnDelete);
        west.add(Box.createVerticalGlue());
        west.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 0));
        return west;
    }

    /**
     * Attempts to obtain the currently selected task id from the list panel.
     * If the panel doesn't expose such API, returns null (we keep logic intact).
     */
    private Integer tryGetSelectedId() {
        try {
            var m = list.getClass().getMethod("getSelectedId");
            Object value = m.invoke(list);
            if (value instanceof Integer i && i > 0) return i;
        } catch (Throwable ignore) {
            // Silent fallback – no API enforced.
        }
        return null;
    }

    // ===== Right filters (East) =====

    /**
     * Hosts the FiltersPanel on the right, plus spacing consistent with the mock.
     */
    private JComponent buildRightFilters() {
        JPanel east = new JPanel();
        east.setBackground(UITheme.BG_APP);
        east.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, UITheme.TABLE_GRID));
        east.setLayout(new BorderLayout());
        east.add(filters.getComponent(), BorderLayout.CENTER);
        return east;
    }

    // ===== Menu =====

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu file = new JMenu("File");

        JMenuItem export = new JMenuItem("Export…");
        export.setMnemonic('E');
        export.setToolTipText("Export tasks to CSV/TXT");
        export.addActionListener(e -> openExportDialog());

        JMenuItem refresh = new JMenuItem("Refresh");
        refresh.addActionListener(e -> safeRun(this::reloadFromVM));

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());

        file.add(export);
        file.addSeparator();
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

    /** Opens the Export dialog (MVVM: dialog talks to ViewModel only). */
    private void openExportDialog() {
        final var dlg = new ExportDialog(this, vm);
        dlg.setVisible(true);
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

    // ===== Actions (wired the same; no signature changes) =====

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
            p = p.and(t -> Objects.equals(t.state(), stateEquals.name()));
        }
        List<TasksViewModel.RowDTO> filtered = masterRows.stream().filter(p).toList();

        // Sort
        Comparator<TasksViewModel.RowDTO> cmp = switch (sortKey) {
            case "title" -> Comparator.comparing(TasksViewModel.RowDTO::title, String.CASE_INSENSITIVE_ORDER);
            case "state" -> Comparator.comparing(TasksViewModel.RowDTO::state);
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
