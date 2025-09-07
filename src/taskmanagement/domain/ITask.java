package taskmanagement.domain;

import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Represents a task entity in the system.
 * <p>
 * Tasks expose their identity, descriptive fields, lifecycle state,
 * and support visiting via the {@link TaskVisitor} interface.
 * </p>
 */
public interface ITask {

    /**
     * Returns the unique identifier of the task.
     *
     * @return task id
     */
    int getId();

    /**
     * Returns the title of the task.
     *
     * @return task title (may be {@code null})
     */
    String getTitle();

    /**
     * Returns the description of the task.
     *
     * @return task description (may be {@code null})
     */
    String getDescription();

    /**
     * Returns the current lifecycle state of the task.
     *
     * @return task state (never {@code null})
     */
    TaskState getState();

    /**
     * Accepts a {@link TaskVisitor}, allowing operations to be
     * performed on this task instance using the Visitor pattern.
     *
     * @param visitor the visitor to accept (must not be {@code null})
     */
    void accept(TaskVisitor visitor);
}
