package taskmanagement.domain.visitor.export;

import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Produces plain-text block list for tasks by visiting ExportNode variants.
 */
public final class PlainTextFlatTaskVisitor implements TaskVisitor {
    private final StringBuilder sb = new StringBuilder("Tasks Export\n------------\n");

    private static String nz(String s) { return s == null ? "" : s; }
    private void addBlock(ExportNode n) {
        sb.append("ID: ").append(n.id()).append('\n')
                .append("Title: ").append(nz(n.title())).append('\n')
                .append("Description: ").append(nz(n.description())).append('\n')
                .append("State: ").append(n.state().name()).append("\n\n");
    }

    @Override public void visit(ToDoTaskRec node) { addBlock(node); }
    @Override public void visit(InProgressTaskRec node) { addBlock(node); }
    @Override public void visit(CompletedTaskRec node) { addBlock(node); }

    /** @return plain text content accumulated so far */
    public String result() { return sb.toString(); }
}
