package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * Command that updates an existing task to a new snapshot.
 * <p>
 * Implements the Command design pattern:
 * <ul>
 *   <li>{@link #execute()} updates the task to the {@code afterSnapshot} state.</li>
 *   <li>{@link #undo()} restores the task to the {@code beforeSnapshot} state.</li>
 * </ul>
 * </p>
 */
public final class UpdateTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask beforeSnapshot;
    private final ITask afterSnapshot;

    /**
     * Constructs a new {@code UpdateTaskCommand}.
     *
     * @param dao            the tasks DAO (must not be {@code null})
     * @param beforeSnapshot snapshot of the entity before update (must not be {@code null})
     * @param afterSnapshot  snapshot of the entity after update (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public UpdateTaskCommand(ITasksDAO dao, ITask beforeSnapshot, ITask afterSnapshot) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.beforeSnapshot = Objects.requireNonNull(beforeSnapshot, "beforeSnapshot");
        this.afterSnapshot = Objects.requireNonNull(afterSnapshot, "afterSnapshot");
    }

    /**
     * Returns the human-readable name of this command.
     *
     * @return the command name
     */
    @Override
    public String name() {
        return "Update Task";
    }

    /**
     * Executes the update by applying the {@code afterSnapshot}.
     * <p>
     * Ensures both snapshots belong to the same task ID before performing the update.
     * </p>
     *
     * @throws TasksDAOException if the IDs mismatch or the DAO update fails
     */
    @Override
    public void execute() throws TasksDAOException {
        if (beforeSnapshot.getId() != afterSnapshot.getId()) {
            throw new TasksDAOException(
                    "Mismatched ids between before/after snapshots: "
                            + beforeSnapshot.getId() + " vs " + afterSnapshot.getId()
            );
        }
        dao.updateTask(afterSnapshot);
    }

    /**
     * Undoes the update by restoring the {@code beforeSnapshot}.
     *
     * @throws TasksDAOException if the DAO update fails
     */
    @Override
    public void undo() throws TasksDAOException {
        dao.updateTask(beforeSnapshot);
    }
}
