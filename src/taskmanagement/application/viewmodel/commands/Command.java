package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.TasksDAOException;

/**
 * A reversible application action (Command pattern).
 * Implementations should carry all data needed to execute and undo safely.
 */
public interface Command {

    /** @return short human-readable name for menus/logging. */
    String name();

    /**
     * Executes the action. Must be idempotent with respect to redo (i.e., redo calls execute again).
     * @throws TasksDAOException if a persistence error occurs
     * @throws ValidationException if domain validation fails
     */
    void execute() throws TasksDAOException, ValidationException;

    /**
     * Reverts the last {@link #execute()} of this command.
     * @throws TasksDAOException if a persistence error occurs
     * @throws ValidationException if domain validation fails
     */
    void undo() throws TasksDAOException, ValidationException;
}
