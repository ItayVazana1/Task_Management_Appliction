package taskmanagement.domain.visitor.export;

import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Produces CSV text (UTF-8) for tasks by visiting ExportNode variants.
 */
public final class CsvFlatTaskVisitor implements TaskVisitor {
    private final StringBuilder sb = new StringBuilder("id,title,description,state\n");

    private static String esc(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    private void addRow(ExportNode n) {
        sb.append(n.id()).append(',')
                .append(esc(n.title())).append(',')
                .append(esc(n.description())).append(',')
                .append(n.state().name())
                .append('\n');
    }

    @Override public void visit(ToDoTaskRec node) { addRow(node); }
    @Override public void visit(InProgressTaskRec node) { addRow(node); }
    @Override public void visit(CompletedTaskRec node) { addRow(node); }

    /** @return CSV content accumulated so far */
    public String result() { return sb.toString(); }
}
