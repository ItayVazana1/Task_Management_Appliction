package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * Command that deletes a task from the DAO and supports undo by restoring it.
 * <p>
 * Implements the Command design pattern:
 * <ul>
 *   <li>{@link #execute()} removes the task from persistence.</li>
 *   <li>{@link #undo()} restores the previously deleted task snapshot.</li>
 * </ul>
 * </p>
 */
public final class DeleteTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask deletedSnapshot;

    /**
     * Constructs a new {@code DeleteTaskCommand}.
     *
     * @param dao             the tasks DAO used for persistence (must not be {@code null})
     * @param deletedSnapshot snapshot of the task to be deleted (must not be {@code null})
     * @throws NullPointerException if {@code dao} or {@code deletedSnapshot} is {@code null}
     */
    public DeleteTaskCommand(ITasksDAO dao, ITask deletedSnapshot) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.deletedSnapshot = Objects.requireNonNull(deletedSnapshot, "deletedSnapshot");
    }

    /**
     * Returns the human-readable name of this command.
     *
     * @return the command name
     */
    @Override
    public String name() {
        return "Delete Task";
    }

    /**
     * Executes the deletion of the task identified by its ID.
     *
     * @throws TasksDAOException if the DAO fails to delete the task
     */
    @Override
    public void execute() throws TasksDAOException {
        dao.deleteTask(deletedSnapshot.getId());
    }

    /**
     * Restores the previously deleted task by re-inserting its snapshot.
     *
     * @throws TasksDAOException if the DAO fails to re-add the task
     */
    @Override
    public void undo() throws TasksDAOException {
        dao.addTask(deletedSnapshot);
    }
}
