package taskmanagement.application.viewmodel.commands;

/**
 * Wraps low-level exceptions (DAO/validation) thrown while executing or undoing a command,
 * so the ViewModel can expose a single checked exception upward to the UI.
 */
public class CommandException extends Exception {

    /** Creates a new CommandException with message only. */
    public CommandException(String message) {
        super(message);
    }

    /** Creates a new CommandException with message and cause. */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Creates a new CommandException with cause only. */
    public CommandException(Throwable cause) {
        super(cause);
    }
}
