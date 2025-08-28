package taskmanagement.domain.exceptions;

/**
 * Thrown when a domain object (e.g., Task) violates validation rules.
 */
public class ValidationException extends Exception {
    /**
     * Creates a new ValidationException with a message.
     * @param message detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates a new ValidationException with a message and root cause.
     * @param message detail message
     * @param cause   root cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
