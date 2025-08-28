package taskmanagement.domain;

import taskmanagement.domain.visitor.ITaskVisitor;

/**
 * Represents a task in the system.
 * <p>MVVM model-side interface. Implementations must be immutable-safe or validate via setters.</p>
 */
public interface ITask {
    /**
     * @return the unique identifier of the task
     */
    int getId();

    /**
     * @return the task title (non-null, non-empty)
     */
    String getTitle();

    /**
     * @return the task description (non-null, may be empty)
     */
    String getDescription();

    /**
     * @return the current state of the task
     */
    TaskState getState();

    /**
     * Accepts a visitor for report/processing logic.
     * @param visitor the visitor to accept
     */
    void accept(ITaskVisitor visitor);
}
