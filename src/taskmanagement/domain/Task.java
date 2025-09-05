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
 *   <li>{@code setState} validates non-null only (keeps backward compatibility).</li>
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
     * @throws IllegalArgumentException if title is blank or state is null
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
    // ITask (public API expected by the rest of the application)
    // ------------------------------------------------------------

    @Override
    public int getId() {
        return id;
    }

    /**
     * Sets the identifier.
     * <p>Note: negative ids are allowed only if your DAO uses them as placeholders.
     * If not, keep ids >= 0.
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    /** Sets a non-blank title. */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
        this.title = title.trim();
    }

    @Override
    public String getDescription() {
        return description;
    }

    /** Sets description (nullable). Trims when non-null. */
    public void setDescription(String description) {
        this.description = (description == null) ? null : description.trim();
    }

    @Override
    public TaskState getState() {
        return state;
    }

    /**
     * Sets state (non-null). Does not enforce transition rules to preserve
     * backward compatibility with existing DAO/VM flows that directly set a state.
     */
    public void setState(TaskState state) {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        this.state = state;
    }

    /**
     * State-pattern helper: transition to {@code target} only if allowed by current state's rules.
     *
     * @param target desired target state
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
     * State-pattern helper: advance to the next state according to the
     * current state's behavior.
     *
     * @throws ValidationException if advancing is not allowed (shouldn't happen with current rules)
     */
    public void advanceState() throws ValidationException {
        transitionTo(Objects.requireNonNull(this.state, "current state is null").next());
    }

    // ------------------------------------------------------------
    // Visitor integration: create a record per state and dispatch
    // ------------------------------------------------------------

    @Override
    public void accept(TaskVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        final TaskState s = Objects.requireNonNull(this.state, "state must not be null");
        switch (s) {
            case ToDo -> visitor.visit(new ToDoTaskRec(id, title, description));
            case InProgress -> visitor.visit(new InProgressTaskRec(id, title, description));
            case Completed -> visitor.visit(new CompletedTaskRec(id, title, description));
        }
    }

    // ------------------------------------------------------------
    // Object contracts
    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        // Identity by id (stable under DAO assignment). If ids are not assigned yet,
        // equals falls back to reference equality above.
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

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
