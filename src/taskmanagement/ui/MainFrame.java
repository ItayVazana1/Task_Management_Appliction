package taskmanagement.ui;

import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.ui.panels.FiltersPanel;
import taskmanagement.ui.panels.TaskListPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

/**
 * Main application window (MVVM View).
 * Connects UI panels to the TasksViewModel without touching the DAO directly.
 */
public final class MainFrame extends JFrame {

    private final TasksViewModel viewModel;
    private final TaskListPanel listPanel;
    private final FiltersPanel filtersPanel;
    private final JLabel statusBar = new JLabel("Ready");

    /**
     * Creates the main window and wires it to the provided ViewModel.
     *
     * @param viewModel the Tasks ViewModel to bind to
     */
    public MainFrame(TasksViewModel viewModel) {
        super("Tasks Management");
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel is required");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top: toolbar
        add(buildToolbar(), BorderLayout.NORTH);

        // Center: tasks list
        listPanel = new TaskListPanel(this.viewModel);
        add(listPanel, BorderLayout.CENTER);

        // West: filters
        filtersPanel = new FiltersPanel(this.viewModel, listPanel::refresh);
        add(filtersPanel, BorderLayout.WEST);

        // Bottom: simple status bar
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusBar, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
    }

    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> listPanel.openAddDialog(this));

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> listPanel.openEditDialog(this));

        JButton delBtn = new JButton("Delete");
        delBtn.addActionListener(e -> listPanel.deleteSelected());

        JButton markTodo = new JButton("ToDo");
        markTodo.addActionListener(e -> listPanel.markSelectedState("ToDo"));

        JButton markInProg = new JButton("InProgress");
        markInProg.addActionListener(e -> listPanel.markSelectedState("InProgress"));

        JButton markDone = new JButton("Completed");
        markDone.addActionListener(e -> listPanel.markSelectedState("Completed"));

        JButton reportBtn = new JButton("Report");
        reportBtn.addActionListener(e -> listPanel.showReport(this));

        tb.add(addBtn);
        tb.add(editBtn);
        tb.add(delBtn);
        tb.addSeparator();
        tb.add(markTodo);
        tb.add(markInProg);
        tb.add(markDone);
        tb.addSeparator();
        tb.add(reportBtn);
        return tb;
    }
}
