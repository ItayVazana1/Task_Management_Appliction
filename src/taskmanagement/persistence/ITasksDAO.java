package taskmanagement.persistence;

import taskmanagement.domain.ITask;

/**
 * Data Access Object (DAO) contract for task persistence.
 * <p>
 * The API follows the project requirements (array-based results).
 * Implementations must not return {@code null} for a missing entity; they
 * should throw {@link TasksDAOException} instead.
 * </p>
 */
public interface ITasksDAO {

    /**
     * Retrieves all tasks currently stored.
     *
     * @return a non-{@code null} array of tasks (may be empty)
     * @throws TasksDAOException if a persistence error occurs
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param id the task identifier
     * @return the matching task (never {@code null})
     * @throws TasksDAOException if the task is not found or a persistence error occurs
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Persists a new task.
     *
     * @param task the task to add
     * @throws TasksDAOException if a persistence error occurs
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Updates an existing task (matched by its identifier).
     *
     * @param task the task containing updated fields
     * @throws TasksDAOException if the task is not found or a persistence error occurs
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Deletes all tasks from the underlying storage.
     *
     * @throws TasksDAOException if the operation fails
     */
    void deleteTasks() throws TasksDAOException;

    /**
     * Deletes a single task by its unique identifier.
     *
     * @param id the task identifier
     * @throws TasksDAOException if the task is not found or a persistence error occurs
     */
    void deleteTask(int id) throws TasksDAOException;
}
