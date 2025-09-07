package taskmanagement.persistence;

/**
 * Exception type for persistence and DAO-related errors specific to
 * the Tasks Management Application.
 * <p>
 * This custom exception ensures that persistence failures are clearly
 * distinguished from generic runtime exceptions.
 * </p>
 */
public class TasksDAOException extends RuntimeException {

    /**
     * Creates a new {@code TasksDAOException} with a descriptive message.
     *
     * @param message the detail message
     */
    public TasksDAOException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code TasksDAOException} with a descriptive message
     * and a root cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause of the exception
     */
    public TasksDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
