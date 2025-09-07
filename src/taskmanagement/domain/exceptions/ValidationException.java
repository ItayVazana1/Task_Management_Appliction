package taskmanagement.domain.exceptions;

/**
 * Exception thrown to indicate validation errors in the domain layer.
 * <p>
 * Used for invalid input values or illegal state transitions within tasks.
 * This exception is unchecked and extends {@link RuntimeException}.
 * </p>
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new {@code ValidationException} with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ValidationException} with the specified detail message
     * and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause (may be {@code null})
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
