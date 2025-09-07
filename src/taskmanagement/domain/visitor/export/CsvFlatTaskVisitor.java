package taskmanagement.domain.visitor.export;

import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Visitor implementation that produces CSV (UTF-8) text output
 * for tasks by visiting {@link ExportNode} variants.
 * <p>
 * Output format:
 * <pre>
 * id,title,description,state
 * 1,"Task A","Description A",ToDo
 * 2,"Task B","Description B",Completed
 * ...
 * </pre>
 * </p>
 */
public final class CsvFlatTaskVisitor implements TaskVisitor {

    private final StringBuilder sb = new StringBuilder("id,title,description,state\n");

    /**
     * Escapes a string for inclusion in CSV by surrounding with quotes
     * and doubling internal quotes.
     *
     * @param s the input string (nullable)
     * @return the escaped string, never {@code null}
     */
    private static String esc(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    /**
     * Appends a row for the given export node.
     *
     * @param n the export node (must not be {@code null})
     */
    private void addRow(ExportNode n) {
        sb.append(n.id()).append(',')
                .append(esc(n.title())).append(',')
                .append(esc(n.description())).append(',')
                .append(n.state().name())
                .append('\n');
    }

    @Override
    public void visit(ToDoTaskRec node) {
        addRow(node);
    }

    @Override
    public void visit(InProgressTaskRec node) {
        addRow(node);
    }

    @Override
    public void visit(CompletedTaskRec node) {
        addRow(node);
    }

    /**
     * Returns the accumulated CSV content.
     *
     * @return CSV text
     */
    public String result() {
        return sb.toString();
    }
}
