package taskmanagement.domain.visitor;

import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.CompletedTaskRec;

/**
 * Base visitor interface over task export record variants.
 * <p>
 * Provides type-specific visit methods for each record implementation,
 * enabling pattern matching on task state. Includes an optional
 * {@link #complete()} hook for batch traversal completion.
 * </p>
 */
public interface ITaskVisitor {

    /**
     * Visits a To-Do task record node.
     *
     * @param node record representing a To-Do task snapshot
     */
    void visit(ToDoTaskRec node);

    /**
     * Visits an In-Progress task record node.
     *
     * @param node record representing an In-Progress task snapshot
     */
    void visit(InProgressTaskRec node);

    /**
     * Visits a Completed task record node.
     *
     * @param node record representing a Completed task snapshot
     */
    void visit(CompletedTaskRec node);

    /**
     * Optional hook invoked after a batch traversal completes.
     * Default implementation is a no-op.
     */
    default void complete() { }
}
