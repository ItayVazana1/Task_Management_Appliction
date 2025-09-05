package taskmanagement.persistence;

import taskmanagement.domain.ITask;

/**
 * Data Access Object (DAO) for tasks persistence.
 * <p>Signatures comply with project Requirements (array-based API).</p>
 */
public interface ITasksDAO {

    /**
     * Returns all tasks currently stored.
     *
     * @return array of tasks (never {@code null}; may be empty)
     * @throws TasksDAOException on persistence failure
     */
    ITask[] getTasks() throws TasksDAOException;

    /**
     * Returns a task by its id.
     *
     * @param id unique task identifier
     * @return the task if found, or {@code null} if not found
     * @throws TasksDAOException on persistence failure
     */
    ITask getTask(int id) throws TasksDAOException;

    /**
     * Persists a new task.
     *
     * @param task task to add (validated by the model)
     * @throws TasksDAOException on persistence failure
     */
    void addTask(ITask task) throws TasksDAOException;

    /**
     * Persists an update to an existing task.
     *
     * @param task task with updated fields (matched by id)
     * @throws TasksDAOException on persistence failure
     */
    void updateTask(ITask task) throws TasksDAOException;

    /**
     * Deletes all tasks from the underlying storage atomically.
     *
     * @throws TasksDAOException if the operation fails (I/O, SQL, etc.)
     */
    void deleteTasks() throws TasksDAOException;

    /**
     * Deletes a single task by id.
     *
     * @param id unique task identifier
     * @throws TasksDAOException on persistence failure
     */
    void deleteTask(int id) throws TasksDAOException;
}
