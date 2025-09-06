package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * {@code MarkStateCommand}
 * <p>
 * Changes a task's lifecycle state by performing a DAO update.
 * <ul>
 *   <li>Execute: build an immutable "after" snapshot with {@code targetState} and update via DAO.</li>
 *   <li>Undo: restore the exact {@code beforeSnapshot} via DAO.</li>
 * </ul>
 * <p>
 * Note: legality of the transition is expected to be validated by the caller (e.g., ViewModel).
 */
public final class MarkStateCommand implements Command {

    /** Factory for creating a new immutable snapshot with a different state. */
    @FunctionalInterface
    public interface TaskFactory {
        ITask copyWithState(ITask src, TaskState newState) throws ValidationException;
    }

    private final ITasksDAO dao;
    private final ITask beforeSnapshot;
    private final TaskState targetState;
    private final TaskFactory factory;

    /** Lazily created on first execute() and reused for redo. */
    private ITask afterSnapshot;

    /**
     * Creates a new {@code MarkStateCommand}.
     *
     * @param dao            tasks DAO (must not be {@code null})
     * @param beforeSnapshot full snapshot of current entity (must not be {@code null})
     * @param targetState    state to apply (must not be {@code null})
     * @param factory        snapshot factory (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public MarkStateCommand(ITasksDAO dao, ITask beforeSnapshot, TaskState targetState, TaskFactory factory) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.beforeSnapshot = Objects.requireNonNull(beforeSnapshot, "beforeSnapshot");
        this.targetState = Objects.requireNonNull(targetState, "targetState");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /** {@inheritDoc} */
    @Override
    public String name() { return "Mark State: " + targetState; }

    /** {@inheritDoc} */
    @Override
    public void execute() throws TasksDAOException, ValidationException {
        if (afterSnapshot == null) {
            afterSnapshot = factory.copyWithState(beforeSnapshot, targetState);
        }
        dao.updateTask(afterSnapshot);
    }

    /** {@inheritDoc} */
    @Override
    public void undo() throws TasksDAOException {
        dao.updateTask(beforeSnapshot);
    }
}
