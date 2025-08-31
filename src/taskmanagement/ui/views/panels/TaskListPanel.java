package taskmanagement.ui.views.panels;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Table of tasks + action buttons (Add, Edit, Delete, Mark state).
 * Stateless regarding data; owner pushes rows via {@link #setRows(List)}.
 */
public final class TaskListPanel {

    private final JPanel root = new JPanel(new BorderLayout());
    private final TasksTableModel model = new TasksTableModel();
    private final JTable table = new JTable(model);

    private final Runnable onAdd;
    private final Consumer<Integer> onEdit;
    private final Consumer<Integer> onDelete;
    private final BiConsumer<Integer, TaskState> onMark;

    public TaskListPanel(Runnable onAdd,
                         Consumer<Integer> onEdit,
                         Consumer<Integer> onDelete,
                         BiConsumer<Integer, TaskState> onMark) {
        this.onAdd = Objects.requireNonNull(onAdd);
        this.onEdit = Objects.requireNonNull(onEdit);
        this.onDelete = Objects.requireNonNull(onDelete);
        this.onMark = Objects.requireNonNull(onMark);
        build();
    }

    public JComponent getComponent() {
        return root;
    }

    public void setRows(List<ITask> tasks) {
        model.setRows(tasks);
    }

    private void build() {
        root.setBackground(UITheme.BASE_900);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Prefer fixed widths for better UX
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // id
        table.getColumnModel().getColumn(1).setPreferredWidth(220);  // title
        table.getColumnModel().getColumn(2).setPreferredWidth(420);  // description
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // state

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton toDo = new JButton("Mark ToDo");
        JButton inProg = new JButton("Mark InProgress");
        JButton done = new JButton("Mark Completed");

        UITheme.styleAddButton(add);
        UITheme.styleDeleteButton(del);
        UITheme.styleFilterButton(edit);
        UITheme.styleFilterButton(toDo);
        UITheme.styleFilterButton(inProg);
        UITheme.styleFilterButton(done);

        buttons.add(add);
        buttons.add(edit);
        buttons.add(del);
        buttons.add(toDo);
        buttons.add(inProg);
        buttons.add(done);

        add.addActionListener(e -> onAdd.run());
        edit.addActionListener(e -> withSelected(onEdit));
        del.addActionListener(e -> withSelected(onDelete));
        toDo.addActionListener(e -> withSelected(id -> onMark.accept(id, TaskState.ToDo)));
        inProg.addActionListener(e -> withSelected(id -> onMark.accept(id, TaskState.InProgress)));
        done.addActionListener(e -> withSelected(id -> onMark.accept(id, TaskState.Completed)));

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
    }

    private void withSelected(Consumer<Integer> action) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        action.accept(id);
    }

    private static final class TasksTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Title", "Description", "State"};
        private List<ITask> rows = new ArrayList<>();

        void setRows(List<ITask> list) {
            rows = new ArrayList<>(list);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ITask t = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> t.getId();
                case 1 -> t.getTitle();
                case 2 -> t.getDescription();
                case 3 -> t.getState();
                default -> null;
            };
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Integer.class;
                case 1, 2 -> String.class;
                case 3 -> TaskState.class;
                default -> Object.class;
            };
        }
    }
}
