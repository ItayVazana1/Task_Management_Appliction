package taskmanagement.domain;

import java.util.Objects;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.domain.visitor.TaskVisitor;

/**
 * Concrete Task model implementing ITask.
 * <p>Follows the style guide: validation in setters, constructors delegate to setters,
 * and overrides equals/hashCode/toString.</p>
 */
public class Task implements ITask {

    /** Unique identifier (non-negative). */
    private int id;

    /** Non-null, non-empty title. */
    private String title;

    /** Non-null description (may be empty). */
    private String description;

    /** Non-null state. */
    private TaskState state;

    /**
     * Primary constructor.
     * @param id          task id (non-negative)
     * @param title       task title (non-null, non-empty)
     * @param description task description (non-null, may be empty)
     * @param state       task state (non-null)
     * @throws ValidationException if any argument is invalid
     */
    public Task(int id, String title, String description, TaskState state) throws ValidationException {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
    }

    /**
     * Convenience constructor with default state = ToDo.
     */
    public Task(int id, String title, String description) throws ValidationException {
        this(id, title, description, TaskState.ToDo);
    }

    @Override
    public int getId() {
        return id;
    }

    /**
     * Sets the task id (non-negative).
     * @throws ValidationException if id is negative
     */
    public void setId(int id) throws ValidationException {
        if (id < 0) {
            throw new ValidationException("id must be non-negative");
        }
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Sets the task title (non-null, non-empty after trim).
     * @throws ValidationException if title is null/empty
     */
    public void setTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("title must be non-empty");
        }
        this.title = title.trim();
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description (non-null; empty allowed).
     * @throws ValidationException if description is null
     */
    public void setDescription(String description) throws ValidationException {
        if (description == null) {
            throw new ValidationException("description must be non-null");
        }
        this.description = description;
    }

    @Override
    public TaskState getState() {
        return state;
    }

    /**
     * Sets the task state (non-null).
     * @throws ValidationException if state is null
     */
    public void setState(TaskState state) throws ValidationException {
        if (state == null) {
            throw new ValidationException("state must be non-null");
        }
        this.state = state;
    }

    @Override
    public void accept(taskmanagement.domain.visitor.TaskVisitor visitor) {
        if (visitor == null) return;
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        // id is the identity; title/description/state may change
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state + '}';
    }
}
