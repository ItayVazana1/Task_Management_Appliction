package taskmanagement.ui.views.panels;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.ui.UITheme;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * TaskListPanel
 * -------------
 * Tasks table panel (no bottom buttons).
 * Visuals match the mock:
 *  - Dark card background
 *  - Fixed columns (ID=60, Title=260, Description=520, State=160)
 *  - Row height = 36
 *  - "State" rendered as colored pill
 * Selection UX:
 *  - No background highlight on selection
 *  - Instead, a thick lime border around the entire selected row
 *
 * Data is pushed via {@link #setRows(List)}.
 */
public final class TaskListPanel {

    private static final int COL_ID_W = 120;
    private static final int COL_TITLE_W = 178;
    private static final int COL_DESC_W = 280;
    private static final int COL_STATE_W = 150;

    private final JPanel root = new JPanel(new BorderLayout());
    private final TasksTableModel model = new TasksTableModel();
    private final HighlightSelectionTable table = new HighlightSelectionTable(model);

    // Callbacks (kept for completeness; owner wires them via selection helpers)
    private final Runnable onAdd;
    private final Consumer<Integer> onEdit;
    private final Consumer<Integer> onDelete;
    private final BiConsumer<Integer, TaskState> onMark;

    /**
     * Creates the task list panel.
     */
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

    /** Root Swing component to add into layouts. */
    public JComponent getComponent() {
        return root;
    }

    /** Replaces current rows and refreshes the table. */
    public void setRows(List<ITask> tasks) {
        model.setRows(tasks);
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    /** Returns the currently selected task id, or null if none. */
    public Integer getSelectedId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object v = model.getValueAt(modelRow, 0);
        return (v instanceof Integer i) ? i : null;
    }

    // ===== UI build =====

    private void build() {
        root.setBackground(UITheme.BG_APP);

        // Table visuals according to mock
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(UITheme.TABLE_GRID);
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.FG_PRIMARY);
        table.setOpaque(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        // Header
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.getTableHeader().setForeground(UITheme.FG_PRIMARY);

        // No automatic resize – we control widths
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Fixed column widths (lock min/max to preferred)
        lockColumnWidth(0, COL_ID_W);
        lockColumnWidth(1, COL_TITLE_W);
        lockColumnWidth(2, COL_DESC_W);
        lockColumnWidth(3, COL_STATE_W);

        // State "pill" renderer
        table.getColumnModel()
                .getColumn(3)
                .setCellRenderer(new StatePillRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(UITheme.BG_CARD);
        sp.setBorder(BorderFactory.createEmptyBorder());
        root.add(sp, BorderLayout.CENTER);
    }

    private void lockColumnWidth(int index, int width) {
        TableColumn c = table.getColumnModel().getColumn(index);
        c.setPreferredWidth(width);
        c.setMinWidth(width);
        c.setMaxWidth(width);
    }

    // ===== Table model =====

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
        @Override public boolean isCellEditable(int r, int c) { return false; }

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

    // ===== Renderer for colored state pills =====

    private static final class StatePillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, false, false, row, column); // force non-selected painting
            lbl.setHorizontalAlignment(CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));

            Color bg = UITheme.PILL_TODO_BG;
            Color fg = Color.WHITE;

            if (value instanceof TaskState st) {
                switch (st) {
                    case ToDo -> { bg = UITheme.PILL_TODO_BG; fg = Color.WHITE; }
                    case InProgress -> { bg = UITheme.PILL_INPROGRESS_BG; fg = Color.BLACK; }
                    case Completed -> { bg = UITheme.PILL_COMPLETED_BG; fg = Color.BLACK; }
                }
                lbl.setText(switch (st) {
                    case ToDo -> "To-Do";
                    case InProgress -> "In Progress";
                    case Completed -> "Completed";
                });
            } else {
                lbl.setText(value == null ? "" : value.toString());
            }

            lbl.setOpaque(true);
            lbl.setBackground(bg);
            lbl.setForeground(fg);
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            return lbl;
        }
    }

    // ===== JTable subclass that draws a lime border around the selected row =====

    /**
     * Custom JTable that avoids changing background on selection and instead
     * paints a thick lime rectangle around the selected row.
     */
    private static final class HighlightSelectionTable extends JTable {
        private static final Stroke SELECTION_STROKE = new BasicStroke(2.0f);

        HighlightSelectionTable(AbstractTableModel model) {
            super(model);
            // Keep selection colors equal to normal background/foreground
            setSelectionBackground(UITheme.BG_CARD);
            setSelectionForeground(UITheme.FG_PRIMARY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int selected = getSelectedRow();
            if (selected < 0) return;

            // Row rect spanning all columns
            Rectangle r = getCellRect(selected, 0, true);
            // Expand to full table width (including columns off-screen)
            r.x = 0;
            r.width = getWidth();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(UITheme.BRAND_LIME);
            g2.setStroke(SELECTION_STROKE);

            // Draw outer rectangle just inside the row bounds
            int pad = 2;
            g2.drawRect(r.x + pad, r.y + pad, r.width - 2 * pad, r.height - 2 * pad);

            g2.dispose();
        }
    }
}
