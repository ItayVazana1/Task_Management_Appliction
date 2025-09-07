package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.ITask;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Objects;

/**
 * A command that adds a new task to the system.
 * <p>
 * This command follows the Command design pattern:
 * <ul>
 *   <li>{@link #execute()} adds the task using the DAO and captures the generated ID.</li>
 *   <li>{@link #undo()} deletes the task created by {@code execute()} using the captured ID.</li>
 * </ul>
 * </p>
 */
public final class AddTaskCommand implements Command {

    private final ITasksDAO dao;
    private final ITask taskToAdd;
    private Integer createdId;

    /**
     * Constructs a new {@code AddTaskCommand}.
     *
     * @param dao       the tasks DAO used to persist the task (must not be {@code null})
     * @param taskToAdd the task instance to be added (must not be {@code null})
     * @throws NullPointerException if {@code dao} or {@code taskToAdd} is {@code null}
     */
    public AddTaskCommand(ITasksDAO dao, ITask taskToAdd) {
        this.dao = Objects.requireNonNull(dao, "dao");
        this.taskToAdd = Objects.requireNonNull(taskToAdd, "taskToAdd");
    }

    /**
     * Returns the name of this command.
     *
     * @return a string describing the command
     */
    @Override
    public String name() {
        return "Add Task";
    }

    /**
     * Executes the command by adding the task via the DAO.
     * <p>
     * After execution, the generated ID must be reflected in {@link ITask#getId()}.
     * If the DAO does not assign a valid ID, a {@link TasksDAOException} is thrown.
     * </p>
     *
     * @throws TasksDAOException if the DAO fails to add the task or returns an invalid ID
     */
    @Override
    public void execute() throws TasksDAOException {
        dao.addTask(taskToAdd);
        createdId = taskToAdd.getId();
        if (createdId == null || createdId < 0) {
            throw new TasksDAOException(
                    "DAO did not assign a valid id to the added task (got: " + createdId + ")"
            );
        }
    }

    /**
     * Undoes the addition of the task by deleting it using the captured ID.
     *
     * @throws TasksDAOException if the captured ID is invalid or the DAO fails to delete the task
     */
    @Override
    public void undo() throws TasksDAOException {
        if (createdId == null || createdId <= 0) {
            throw new TasksDAOException("Cannot undo add: missing valid createdId");
        }
        dao.deleteTask(createdId);
    }
}
