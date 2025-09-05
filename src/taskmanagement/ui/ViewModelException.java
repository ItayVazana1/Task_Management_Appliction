package taskmanagement.ui;

/**
 * Generic exception type for ViewModel-facing operations.
 * Wraps domain/persistence exceptions so the View layer doesn't depend on them directly.
 */
public class ViewModelException extends Exception {
    public ViewModelException(String message) { super(message); }
    public ViewModelException(String message, Throwable cause) { super(message, cause); }
}
