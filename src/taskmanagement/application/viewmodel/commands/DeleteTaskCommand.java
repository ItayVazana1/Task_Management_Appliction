package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * {@code DeleteTaskCommand}
 * <p>
 * A concrete implementation of the Command pattern that deletes a task
 * from the DAO and supports undo by re-inserting the previously deleted task.
 */
public final class DeleteTaskCommand implements Command {

    /** DAO used for persistence operations. */
    private final ITasksDAO dao;

    /** Snapshot of the task that was deleted, required for undo. */
    private final ITask deletedSnapshot;

    /**
     * Creates a new {@code DeleteTaskCommand}.
     *
     * @param dao             tasks DAO (must not be {@code null})
     * @param deletedSnapshot full snapshot of the task being deleted (must not be {@code null})
     */
    public DeleteTaskCommand(ITasksDAO dao, ITask deletedSnapshot) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.deletedSnapshot = Objects.requireNonNull(deletedSnapshot, "deletedSnapshot");
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return "Delete Task";
    }

    /**
     * Executes the deletion of the task using its id.
     *
     * @throws TasksDAOException if the DAO cannot delete the task
     */
    @Override
    public void execute() throws TasksDAOException {
        dao.deleteTask(deletedSnapshot.getId());
    }

    /**
     * Reverts the deletion by re-adding the previously deleted task snapshot.
     *
     * @throws TasksDAOException if the DAO cannot re-insert the task
     */
    @Override
    public void undo() throws TasksDAOException {
        dao.addTask(deletedSnapshot);
    }
}
