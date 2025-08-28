package taskmanagement.domain.visitor;

import taskmanagement.domain.ITask;

/**
 * Visitor for processing tasks (e.g., reporting).
 * <p>Kept minimal for now; can be extended alongside record-based reports later.</p>
 */
@FunctionalInterface
public interface ITaskVisitor {
    /**
     * Visit a single task.
     * @param task the task to visit
     */
    void visit(ITask task);
}
