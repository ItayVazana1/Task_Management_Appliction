package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.TasksDAOException;

/**
 * Represents a reversible operation in the application.
 */
public interface Command {

    /**
     * @return a short human-readable name.
     */
    String name();

    /**
     * Executes the command.
     *
     * @throws TasksDAOException on DAO failures
     * @throws ValidationException on validation failures
     */
    void execute() throws TasksDAOException, ValidationException;

    /**
     * Reverts the side effects of {@link #execute()}.
     *
     * @throws TasksDAOException on DAO failures
     * @throws ValidationException on validation failures
     */
    void undo() throws TasksDAOException, ValidationException;
}
