package taskmanagement.domain.visitor;

import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.CompletedTaskRec;

/**
 * Base visitor over task export record variants.
 * Each visit method handles a specific record (pattern-matching by type).
 */
public interface ITaskVisitor {

    /**
     * Visit a To-Do task record node.
     * @param node record representing a To-Do task snapshot
     */
    void visit(ToDoTaskRec node);

    /**
     * Visit an In-Progress task record node.
     * @param node record representing an In-Progress task snapshot
     */
    void visit(InProgressTaskRec node);

    /**
     * Visit a Completed task record node.
     * @param node record representing a Completed task snapshot
     */
    void visit(CompletedTaskRec node);

    /**
     * Optional hook invoked after a batch traversal.
     * Implementations may no-op.
     */
    default void complete() { }
}
