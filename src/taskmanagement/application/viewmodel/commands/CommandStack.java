package taskmanagement.application.viewmodel.commands;

import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.TasksDAOException;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo/Redo stack for application commands.
 * Push via {@link #execute(Command)}; call {@link #undo()} or {@link #redo()} as needed.
 */
public final class CommandStack {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Executes a command, pushes it onto the undo stack, and clears the redo stack.
     * @param command the command to execute
     * @throws CommandException if execution fails
     */
    public void execute(Command command) throws CommandException {
        try {
            command.execute();
            undoStack.push(command);
            redoStack.clear();
        } catch (TasksDAOException | ValidationException ex) {
            throw new CommandException("Failed to execute command: " + command.name(), ex);
        }
    }

    /**
     * Undoes the last executed command and pushes it to the redo stack.
     * No-op if there is nothing to undo.
     * @throws CommandException if undo fails
     */
    public void undo() throws CommandException {
        if (undoStack.isEmpty()) return;
        Command c = undoStack.pop();
        try {
            c.undo();
            redoStack.push(c);
        } catch (TasksDAOException | ValidationException ex) {
            // restore stack invariants on failure
            undoStack.push(c);
            throw new CommandException("Failed to undo command: " + c.name(), ex);
        }
    }

    /**
     * Redoes the last undone command (re-executes it) and pushes it back to undo.
     * No-op if there is nothing to redo.
     * @throws CommandException if redo fails
     */
    public void redo() throws CommandException {
        if (redoStack.isEmpty()) return;
        Command c = redoStack.pop();
        try {
            c.execute();
            undoStack.push(c);
        } catch (TasksDAOException | ValidationException ex) {
            // restore stack invariants on failure
            redoStack.push(c);
            throw new CommandException("Failed to redo command: " + c.name(), ex);
        }
    }

    /** @return true if at least one command can be undone. */
    public boolean canUndo() { return !undoStack.isEmpty(); }

    /** @return true if at least one command can be redone. */
    public boolean canRedo() { return !redoStack.isEmpty(); }

    /** Clears both stacks (e.g., on project reset). */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /** @return number of commands in the undo stack (useful for testing/diagnostics). */
    public int undoCount() { return undoStack.size(); }

    /** @return number of commands in the redo stack (useful for testing/diagnostics). */
    public int redoCount() { return redoStack.size(); }
}
