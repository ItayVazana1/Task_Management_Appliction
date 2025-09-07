package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.TasksDAOException;

/**
 * Represents a reversible application action following the Command pattern.
 * <p>
 * Each command encapsulates all data required to perform an operation and
 * to safely undo it.
 * </p>
 */
public interface Command {

    /**
     * Returns a short human-readable name of this command.
     * Useful for menus, logs, or debugging.
     *
     * @return the command name
     */
    String name();

    /**
     * Executes the command.
     * <p>
     * Implementations must be idempotent with respect to redo,
     * meaning calling {@code execute()} again after an undo should
     * have the same effect as the initial execution.
     * </p>
     *
     * @throws TasksDAOException     if a persistence-related error occurs
     * @throws ValidationException   if domain validation fails
     */
    void execute() throws TasksDAOException, ValidationException;

    /**
     * Undoes the last successful execution of this command.
     *
     * @throws TasksDAOException     if a persistence-related error occurs
     * @throws ValidationException   if domain validation fails
     */
    void undo() throws TasksDAOException, ValidationException;
}
