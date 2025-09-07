package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * Command that changes the lifecycle state of a task.
 * <p>
 * Implements the Command design pattern:
 * <ul>
 *   <li>{@link #execute()} updates the task to the target state using the DAO.</li>
 *   <li>{@link #undo()} restores the original state using the stored snapshot.</li>
 * </ul>
 * </p>
 * <p>
 * Validation of whether the transition is legal must be performed
 * externally by the caller (e.g., in the ViewModel).
 * </p>
 */
public final class MarkStateCommand implements Command {

    /**
     * Factory for producing a new immutable task snapshot with a modified state.
     */
    @FunctionalInterface
    public interface TaskFactory {
        /**
         * Creates a copy of the given task with a new state applied.
         *
         * @param src       the original task
         * @param newState  the new state to assign
         * @return a new task snapshot with the updated state
         * @throws ValidationException if the new state is invalid for the task
         */
        ITask copyWithState(ITask src, TaskState newState) throws ValidationException;
    }

    private final ITasksDAO dao;
    private final ITask beforeSnapshot;
    private final TaskState targetState;
    private final TaskFactory factory;

    private ITask afterSnapshot;

    /**
     * Constructs a new {@code MarkStateCommand}.
     *
     * @param dao            the DAO for persistence operations (must not be {@code null})
     * @param beforeSnapshot snapshot of the task before modification (must not be {@code null})
     * @param targetState    the target state to apply (must not be {@code null})
     * @param factory        factory for creating modified task snapshots (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public MarkStateCommand(ITasksDAO dao, ITask beforeSnapshot, TaskState targetState, TaskFactory factory) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.beforeSnapshot = Objects.requireNonNull(beforeSnapshot, "beforeSnapshot");
        this.targetState = Objects.requireNonNull(targetState, "targetState");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /**
     * Returns the human-readable name of this command.
     *
     * @return the command name including the target state
     */
    @Override
    public String name() {
        return "Mark State: " + targetState;
    }

    /**
     * Executes the command by updating the task to the target state.
     * If not already created, generates the "after" snapshot using the factory.
     *
     * @throws TasksDAOException     if the DAO update fails
     * @throws ValidationException   if the factory produces an invalid task
     */
    @Override
    public void execute() throws TasksDAOException, ValidationException {
        if (afterSnapshot == null) {
            afterSnapshot = factory.copyWithState(beforeSnapshot, targetState);
        }
        dao.updateTask(afterSnapshot);
    }

    /**
     * Undoes the state change by restoring the original snapshot.
     *
     * @throws TasksDAOException if the DAO update fails
     */
    @Override
    public void undo() throws TasksDAOException {
        dao.updateTask(beforeSnapshot);
    }
}
