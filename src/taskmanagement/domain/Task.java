package taskmanagement.domain;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.domain.visitor.TaskVisitor;

import java.util.Objects;

/**
 * Represents a single task in the system.
 * <p>
 * Style highlights:
 * <ul>
 *   <li>Private fields</li>
 *   <li>Constructors call setters (validation happens in setters)</li>
 *   <li>Overrides equals/hashCode/toString</li>
 *   <li>No UI or persistence concerns (MVVM separation)</li>
 * </ul>
 */
public class Task implements ITask {

    /** Unique positive identifier of the task. */
    private int id;

    /** Non-empty title (trimmed). */
    private String title;

    /** Optional description (never {@code null}; empty string if unspecified). */
    private String description;

    /** Current state of the task (must be non-null). */
    private TaskState state;

    /**
     * Creates a new {@code Task}.
     *
     * @param id          positive identifier
     * @param title       non-blank title
     * @param description optional description (null becomes empty string)
     * @param state       non-null state
     * @throws ValidationException if any argument is invalid or transition is illegal
     */
    public Task(int id, String title, String description, TaskState state) throws ValidationException {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state); // constructors MUST call setters
    }

    /** {@inheritDoc} */
    @Override
    public int getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public TaskState getState() {
        return state;
    }

    /**
     * Accepts a {@link TaskVisitor} (Visitor pattern).
     * @param visitor concrete visitor to apply
     */
    @Override
    public void accept(TaskVisitor visitor) {
        visitor.visit(this);
    }

    // ---------- validation setters (constructors must call setters) ----------

    /**
     * Sets the id. Must be positive.
     * @param id positive id
     * @throws ValidationException if {@code id <= 0}
     */
    private void setId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("id must be positive");
        }
        this.id = id;
    }

    /**
     * Sets the title. Must be non-null and non-blank.
     * @param title non-blank title
     * @throws ValidationException if title is null/blank
     */
    private void setTitle(String title) throws ValidationException {
        if (title == null || title.isBlank()) {
            throw new ValidationException("title is required");
        }
        this.title = title.trim();
    }

    /**
     * Sets the description. {@code null} becomes empty string.
     * @param description optional description
     */
    private void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    /**
     * Changes task state if lifecycle rules allow it.
     * Constructors call this setter as well.
     *
     * @param state desired state (must not be null)
     * @throws ValidationException if state is null or transition is illegal
     */
    public void setState(TaskState state) throws ValidationException {
        if (state == null) {
            throw new ValidationException("state is required");
        }
        if (this.state == null) {
            // initial assignment (during construction or DB hydration) is allowed
            this.state = state;
            return;
        }
        if (!this.state.canTransitionTo(state)) {
            throw new ValidationException("Illegal state transition: " + this.state + " -> " + state);
        }
        this.state = state;
    }

    // ---------- Object overrides ----------

    /**
     * Two tasks are equal if they share the same {@code id}.
     * @param o other object
     * @return {@code true} if same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task t)) return false; // Java 24 pattern matching for instanceof
        return id == t.id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return id + ":" + title + " [" + state + "]";
    }
}
