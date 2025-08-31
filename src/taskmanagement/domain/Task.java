package taskmanagement.domain;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.domain.visitor.TaskVisitor;
// ⬇️ ייבוא הרקורדים ל-Visitor (ייצוא/דוחות)
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;

import java.util.Objects;

/**
 * Represents a single task in the system.
 * Validation is performed in setters; constructors must call setters.
 * For new (unsaved) tasks, id==0 is allowed as a placeholder (DAO will assign a real id).
 */
public class Task implements ITask {

    /** Identifier: 0 for unsaved placeholder; positive after persistence. */
    private int id;

    /** Non-empty title (trimmed). */
    private String title;

    /** Optional description (never null; empty string if unspecified). */
    private String description;

    /** Current state (non-null). */
    private TaskState state;

    /**
     * Primary constructor.
     *
     * @param id          0 (unsaved) or positive (persisted)
     * @param title       non-blank
     * @param description optional (null -> "")
     * @param state       non-null
     * @throws ValidationException if arguments are invalid or transition is illegal
     */
    public Task(int id, String title, String description, TaskState state) throws ValidationException {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
    }

    @Override
    public int getId() { return id; }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getDescription() { return description; }

    @Override
    public TaskState getState() { return state; }

    /**
     * Accepts a visitor (Visitor pattern).
     * Maps this Task to a record by state and dispatches to the visitor.
     */
    @Override
    public void accept(TaskVisitor visitor) {
        // Pattern matching by TaskState via distinct record variants:
        switch (getState()) {
            case ToDo -> visitor.visit(new ToDoTaskRec(getId(), getTitle(), getDescription()));
            case InProgress -> visitor.visit(new InProgressTaskRec(getId(), getTitle(), getDescription()));
            case Completed -> visitor.visit(new CompletedTaskRec(getId(), getTitle(), getDescription()));
        }
    }

    // ------------ validation setters ------------

    /**
     * Sets the id.
     * 0 is allowed to represent an unsaved task; positive means persisted.
     *
     * @param id 0 or positive
     * @throws ValidationException if id is negative
     */
    private void setId(int id) throws ValidationException {
        if (id < 0) {
            throw new ValidationException("id must be zero (unsaved) or positive");
        }
        this.id = id;
    }

    /**
     * Sets the title (non-blank).
     */
    private void setTitle(String title) throws ValidationException {
        if (title == null || title.isBlank()) {
            throw new ValidationException("title is required");
        }
        this.title = title.trim();
    }

    /**
     * Sets the description (null -> "").
     */
    private void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    /**
     * Changes state while enforcing lifecycle rules.
     */
    public void setState(TaskState state) throws ValidationException {
        if (state == null) {
            throw new ValidationException("state is required");
        }
        if (this.state == null) {
            this.state = state; // initial assignment
            return;
        }
        if (!this.state.canTransitionTo(state)) {
            throw new ValidationException("Illegal state transition: " + this.state + " -> " + state);
        }
        this.state = state;
    }

    // ------------ Object overrides ------------

    /**
     * Equality:
     * - If both ids are positive → compare by id.
     * - If either id is 0 (unsaved) → fall back to reference equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task t)) return false;
        if (this.id == 0 || t.id == 0) {
            // Avoid considering two different unsaved tasks (id==0) as equal
            return false;
        }
        return this.id == t.id;
    }

    /**
     * Hash code:
     * - Positive id → hash by id.
     * - Unsaved (id==0) → identity-based hash to avoid collisions between new tasks.
     */
    @Override
    public int hashCode() {
        return (id == 0) ? System.identityHashCode(this) : Objects.hash(id);
    }

    @Override
    public String toString() {
        return (id == 0 ? "NEW" : String.valueOf(id)) + ":" + title + " [" + state + "]";
    }
}
