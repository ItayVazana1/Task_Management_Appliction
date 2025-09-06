package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * {@code UpdateTaskCommand}
 * <p>
 * Updates an existing task to the {@code afterSnapshot}.
 * <ul>
 *   <li>Execute: {@code dao.updateTask(afterSnapshot)}</li>
 *   <li>Undo:    {@code dao.updateTask(beforeSnapshot)}</li>
 * </ul>
 */
public final class UpdateTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask beforeSnapshot;
    private final ITask afterSnapshot;

    /**
     * Creates a new {@code UpdateTaskCommand}.
     *
     * @param dao            tasks DAO (must not be {@code null})
     * @param beforeSnapshot full snapshot of the entity before the update (must not be {@code null})
     * @param afterSnapshot  full snapshot to persist during execute() (must not be {@code null})
     * @throws NullPointerException if any argument is {@code null}
     */
    public UpdateTaskCommand(ITasksDAO dao, ITask beforeSnapshot, ITask afterSnapshot) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.beforeSnapshot = Objects.requireNonNull(beforeSnapshot, "beforeSnapshot");
        this.afterSnapshot = Objects.requireNonNull(afterSnapshot, "afterSnapshot");
    }

    /** {@inheritDoc} */
    @Override
    public String name() { return "Update Task"; }

    /** {@inheritDoc} */
    @Override
    public void execute() throws TasksDAOException {
        // Defensive: both snapshots must refer to the same entity id
        if (beforeSnapshot.getId() != afterSnapshot.getId()) {
            throw new TasksDAOException("Mismatched ids between before/after snapshots: "
                    + beforeSnapshot.getId() + " vs " + afterSnapshot.getId());
        }
        dao.updateTask(afterSnapshot);
    }

    /** {@inheritDoc} */
    @Override
    public void undo() throws TasksDAOException {
        dao.updateTask(beforeSnapshot);
    }
}
