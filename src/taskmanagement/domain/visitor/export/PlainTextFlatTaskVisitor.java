package taskmanagement.domain.visitor.export;

import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Visitor implementation that produces plain-text block output
 * for tasks by visiting {@link ExportNode} variants.
 * <p>
 * Output format example:
 * <pre>
 * Tasks Export
 * ------------
 * ID: 1
 * Title: Task A
 * Description: Some description
 * State: ToDo
 *
 * ID: 2
 * Title: Task B
 * Description: Another description
 * State: Completed
 * </pre>
 * </p>
 */
public final class PlainTextFlatTaskVisitor implements TaskVisitor {

    private final StringBuilder sb = new StringBuilder("Tasks Export\n------------\n");

    /**
     * Returns a non-null string (empty if {@code s} is {@code null}).
     *
     * @param s input string (nullable)
     * @return non-null string
     */
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /**
     * Appends a formatted block for the given export node.
     *
     * @param n the export node (must not be {@code null})
     */
    private void addBlock(ExportNode n) {
        sb.append("ID: ").append(n.id()).append('\n')
                .append("Title: ").append(nz(n.title())).append('\n')
                .append("Description: ").append(nz(n.description())).append('\n')
                .append("State: ").append(n.state().name()).append("\n\n");
    }

    @Override
    public void visit(ToDoTaskRec node) {
        addBlock(node);
    }

    @Override
    public void visit(InProgressTaskRec node) {
        addBlock(node);
    }

    @Override
    public void visit(CompletedTaskRec node) {
        addBlock(node);
    }

    /**
     * Returns the accumulated plain-text output.
     *
     * @return plain-text export result
     */
    public String result() {
        return sb.toString();
    }
}
