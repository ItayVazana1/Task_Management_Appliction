package taskmanagement.domain.visitor;

/**
 * Visitor interface required by the project (see Requirements.md).
 * Extends {@link ITaskVisitor} so {@code ITask.accept(TaskVisitor)} works
 * while keeping a single set of visit methods.
 */
public interface TaskVisitor extends ITaskVisitor {
    // No extra members; this type exists to match the required signature.
}
