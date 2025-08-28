package taskmanagement.persistence;

/**
 * Project-specific exception for persistence/DAO errors.
 */
public class TasksDAOException extends RuntimeException {
    public TasksDAOException(String message) { super(message); }
    public TasksDAOException(String message, Throwable cause) { super(message, cause); }
}
