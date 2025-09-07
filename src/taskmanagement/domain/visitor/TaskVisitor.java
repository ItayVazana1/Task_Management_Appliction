package taskmanagement.domain.visitor;

/**
 * Visitor interface required by the project (see Requirements.md).
 * <p>
 * Extends {@link ITaskVisitor} so that domain tasks can call
 * {@code ITask.accept(TaskVisitor)} with a unified visitor type.
 * No additional methods are defined; this type exists to satisfy
 * the required interface signature in the project specification.
 * </p>
 */
public interface TaskVisitor extends ITaskVisitor {
    // Marker interface extending ITaskVisitor
}
