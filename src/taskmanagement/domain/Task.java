package taskmanagement.domain;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.domain.visitor.TaskVisitor;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;

import java.util.Objects;

/**
 * Immutable-identity Task entity (id) with validated fields and
 * a behavioral state (State pattern via {@link TaskState} enum).
 *
 * <p>Notes:
 * <ul>
 *   <li>Constructors call setters to reuse validation.</li>
 *   <li>{@code setState} validates non-null and enforces forward-only transitions.</li>
 *   <li>State-pattern helpers {@link #transitionTo(TaskState)} and {@link #advanceState()} enforce legal transitions.</li>
 * </ul>
 */
public class Task implements ITask {

    private int id;
    private String title;
    private String description;
    private TaskState state;

    /**
     * Creates a task with all fields. Setters perform validation.
     *
     * @param id          task id (>= 0 recommended; DAO may assign)
     * @param title       non-empty title
     * @param description nullable description
     * @param state       non-null state
     * @throws IllegalArgumentException if title is blank
     * @throws ValidationException      if state is null or transition is illegal
     */
    public Task(int id, String title, String description, TaskState state) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
    }

    /** Copy constructor. */
    public Task(Task other) {
        this(Objects.requireNonNull(other, "other must not be null").id,
                other.title, other.description, other.state);
    }

    // ------------------------------------------------------------
    // ITask
    // ------------------------------------------------------------

    @Override public int getId() { return id; }

    /**
     * Sets the identifier.
     * <p>Note: negative ids are allowed only if your DAO uses them as placeholders.
     * If not, keep ids >= 0.
     */
    public void setId(int id) { this.id = id; }

    @Override public String getTitle() { return title; }

    /** Sets a non-blank title. */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
        this.title = title.trim();
    }

    @Override public String getDescription() { return description; }

    /** Sets description (nullable). Trims when non-null. */
    public void setDescription(String description) {
        this.description = (description == null) ? null : description.trim();
    }

    @Override public TaskState getState() { return state; }

    /**
     * Sets state (non-null) and enforces forward-only transitions.
     * Allowed: ToDo → InProgress → Completed (self-sets are idempotent).
     *
     * <p><b>No signature change:</b> throws an unchecked {@link ValidationException}
     * to avoid ripple effects across callers.</p>
     *
     * @param newState the new state (must not be null)
     * @throws ValidationException if newState is null or the transition is illegal
     */
    public void setState(TaskState newState) {
        if (newState == null) {
            throw new ValidationException("state must not be null");
        }
        if (this.state == newState) {
            return; // idempotent
        }
        if (this.state != null && !this.state.canTransitionTo(newState)) {
            throw new ValidationException("Illegal state transition: " + this.state + " -> " + newState);
        }
        this.state = newState;
    }

    /**
     * Transition helper using State pattern rules.
     *
     * @param target desired target state (must not be null)
     * @throws ValidationException if transition is not allowed
     * @throws NullPointerException if target is null
     */
    public void transitionTo(TaskState target) throws ValidationException {
        Objects.requireNonNull(target, "target must not be null");
        if (this.state == null || this.state.canTransitionTo(target)) {
            this.state = target;
        } else {
            throw new ValidationException("Illegal state transition: " + this.state + " -> " + target);
        }
    }

    /**
     * Advance to the next state per current state's behavior.
     *
     * @throws ValidationException if advancing is not allowed
     */
    public void advanceState() throws ValidationException {
        transitionTo(Objects.requireNonNull(this.state, "current state is null").next());
    }

    // ------------------------------------------------------------
    // Visitor
    // ------------------------------------------------------------

    @Override
    public void accept(TaskVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        final TaskState s = Objects.requireNonNull(this.state, "state must not be null");
        switch (s) {
            case ToDo       -> visitor.visit(new ToDoTaskRec(id, title, description));
            case InProgress -> visitor.visit(new InProgressTaskRec(id, title, description));
            case Completed  -> visitor.visit(new CompletedTaskRec(id, title, description));
        }
    }

    // ------------------------------------------------------------
    // Object contracts
    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        return id == other.id;
    }

    @Override public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                '}';
    }
}
