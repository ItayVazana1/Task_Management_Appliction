package taskmanagement.persistence;

/**
 * Project-specific exception for DAO operations in the Tasks module.
 */
public class TasksDAOException extends Exception {

    /**
     * Creates a new TasksDAOException with a message.
     * @param message detail message
     */
    public TasksDAOException(String message) {
        super(message);
    }

    /**
     * Creates a new TasksDAOException with a message and a root cause.
     * @param message detail message
     * @param cause   root cause
     */
    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
