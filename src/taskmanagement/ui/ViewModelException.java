package taskmanagement.ui;

/**
 * Exception type for errors that occur in the ViewModel layer.
 * <p>
 * This project-specific checked exception wraps lower-level domain
 * or persistence exceptions so that the UI layer does not directly
 * depend on them.
 * </p>
 */
public class ViewModelException extends Exception {

    /**
     * Constructs a new {@code ViewModelException} with the specified detail message.
     *
     * @param message the detail message, saved for later retrieval by {@link #getMessage()}
     */
    public ViewModelException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ViewModelException} with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the cause (which is saved for later retrieval by {@link #getCause});
     *                a {@code null} value is permitted
     */
    public ViewModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
