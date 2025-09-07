package taskmanagement.domain.exceptions;

/**
 * ValidationException
 * Unchecked domain exception for invalid input or illegal state transitions.
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates a new ValidationException with the specified detail message.
     * @param message detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates a new ValidationException with the specified detail message and cause.
     * @param message detail message
     * @param cause   the cause (may be null)
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
