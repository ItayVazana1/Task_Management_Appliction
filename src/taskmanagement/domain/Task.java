package taskmanagement.domain;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.domain.visitor.TaskVisitor;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;

import java.util.Objects;

/**
 * Task entity with immutable identity (id), validated fields, and a behavioral state
 * managed by the {@link TaskState} state machine.
 * <p>
 * Constructors delegate to setters to reuse validation logic. State changes are validated
 * to allow only legal forward transitions, and visitor support is provided via
 * {@link #accept(TaskVisitor)}.
 * </p>
 */
public class Task implements ITask {

    private int id;
    private String title;
    private String description;
    private TaskState state;

    /**
     * Constructs a task with all fields; setters perform validation.
     *
     * @param id          task id (non-negative recommended; DAO may assign)
     * @param title       non-empty title
     * @param description nullable description
     * @param state       non-null state
     * @throws IllegalArgumentException if {@code title} is blank
     * @throws ValidationException      if {@code state} is {@code null} or transition is illegal
     */
    public Task(int id, String title, String description, TaskState state) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
    }

    /**
     * Copy constructor.
     *
     * @param other another task instance to copy (must not be {@code null})
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public Task(Task other) {
        this(Objects.requireNonNull(other, "other must not be null").id,
                other.title, other.description, other.state);
    }

    // ------------------------------------------------------------
    // ITask
    // ------------------------------------------------------------

    /** {@inheritDoc} */
    @Override public int getId() { return id; }

    /**
     * Sets the identifier.
     * <p>
     * Negative ids are tolerated only if your DAO uses them as placeholders;
     * otherwise prefer non-negative ids.
     * </p>
     *
     * @param id the identifier to set
     */
    public void setId(int id) { this.id = id; }

    /** {@inheritDoc} */
    @Override public String getTitle() { return title; }

    /**
     * Sets a non-blank title.
     *
     * @param title the title to set (must not be {@code null} or blank)
     * @throws IllegalArgumentException if {@code title} is {@code null} or blank
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
        this.title = title.trim();
    }

    /** {@inheritDoc} */
    @Override public String getDescription() { return description; }

    /**
     * Sets the description (nullable). Trims when non-null.
     *
     * @param description description text, or {@code null}
     */
    public void setDescription(String description) {
        this.description = (description == null) ? null : description.trim();
    }

    /** {@inheritDoc} */
    @Override public TaskState getState() { return state; }

    /**
     * Sets the state and enforces forward-only transitions.
     * <p>
     * Allowed transitions: ToDo → InProgress → Completed. Setting the same state is
     * idempotent. Throws unchecked {@link ValidationException} to keep caller APIs simple.
     * </p>
     *
     * @param newState the new state (must not be {@code null})
     * @throws ValidationException if {@code newState} is {@code null} or the transition is illegal
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
     * Transitions to a target state if allowed by the current state's rules.
     *
     * @param target desired target state (must not be {@code null})
     * @throws ValidationException  if the transition is not allowed
     * @throws NullPointerException if {@code target} is {@code null}
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
     * Advances to the next state according to the current state's behavior.
     *
     * @throws ValidationException if advancing is not allowed
     */
    public void advanceState() throws ValidationException {
        transitionTo(Objects.requireNonNull(this.state, "current state is null").next());
    }

    // ------------------------------------------------------------
    // Visitor
    // ------------------------------------------------------------

    /**
     * Accepts a {@link TaskVisitor} and dispatches to the record-typed visit
     * based on the current {@link TaskState}.
     *
     * @param visitor the visitor to accept (must not be {@code null})
     * @throws NullPointerException if {@code visitor} or {@code state} is {@code null}
     */
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

    /**
     * Compares tasks by identity (id) only.
     *
     * @param o other object
     * @return {@code true} if the other object is a {@code Task} with the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task other)) return false;
        return id == other.id;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() { return Integer.hashCode(id); }

    /**
     * Returns a string representation of the task.
     *
     * @return string form including id, title, description, and state
     */
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
