package taskmanagement.domain;

import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Represents a task in the system.
 */
public interface ITask {
    int getId();
    String getTitle();
    String getDescription();
    TaskState getState();

    void accept(TaskVisitor visitor);
}
