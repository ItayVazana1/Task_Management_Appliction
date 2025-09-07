package taskmanagement.application.viewmodel.commands;

/**
 * Exception type representing errors that occur during command execution or undo.
 * <p>
 * This exception wraps lower-level exceptions (such as DAO or validation errors)
 * so that the ViewModel can propagate a single checked exception type to the UI.
 * </p>
 */
public class CommandException extends Exception {

    /**
     * Constructs a new {@code CommandException} with the specified detail message.
     *
     * @param message the detail message
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CommandException} with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause of this exception
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code CommandException} with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public CommandException(Throwable cause) {
        super(cause);
    }
}
