package taskmanagement.application.viewmodel.commands;

import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.TasksDAOException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Undo/Redo stack for application commands.
 * <p>Push via {@link #execute(Command)}; call {@link #undo()} / {@link #redo()} as needed.</p>
 * <p>Exposes both pull-style flags ({@link #canUndo()}, {@link #canRedo()})
 * and push-style observables ({@link #canUndoProperty()}, {@link #canRedoProperty()}).</p>
 */
public final class CommandStack {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /** Observable flags for UI binding (Observer pattern). */
    private final Property<Boolean> canUndoProp = new Property<>(false);
    private final Property<Boolean> canRedoProp = new Property<>(false);

    /**
     * Executes a command, pushes it onto the undo stack, and clears the redo stack.
     * @param command the command to execute (must not be {@code null})
     * @throws CommandException if execution fails
     */
    public void execute(Command command) throws CommandException {
        Objects.requireNonNull(command, "command");
        try {
            command.execute();
            undoStack.push(command);
            redoStack.clear();
            refreshFlags();
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
            refreshFlags();
        } catch (TasksDAOException | ValidationException ex) {
            // restore stack invariants on failure
            undoStack.push(c);
            refreshFlags();
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
            refreshFlags();
        } catch (TasksDAOException | ValidationException ex) {
            // restore stack invariants on failure
            redoStack.push(c);
            refreshFlags();
            throw new CommandException("Failed to redo command: " + c.name(), ex);
        }
    }

    /** @return true if at least one command can be undone. */
    public boolean canUndo() { return !undoStack.isEmpty(); }

    /** @return true if at least one command can be redone. */
    public boolean canRedo() { return !redoStack.isEmpty(); }

    /** @return observable property for enabling/disabling Undo button in UI. */
    public Property<Boolean> canUndoProperty() { return canUndoProp; }

    /** @return observable property for enabling/disabling Redo button in UI. */
    public Property<Boolean> canRedoProperty() { return canRedoProp; }

    /** Clears both stacks (e.g., on project reset) and updates flags. */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        refreshFlags();
    }

    /** @return number of commands in the undo stack (useful for testing/diagnostics). */
    public int undoCount() { return undoStack.size(); }

    /** @return number of commands in the redo stack (useful for testing/diagnostics). */
    public int redoCount() { return redoStack.size(); }

    /** Recomputes and publishes canUndo/canRedo flags. */
    private void refreshFlags() {
        canUndoProp.setValue(!undoStack.isEmpty());
        canRedoProp.setValue(!redoStack.isEmpty());
    }
}
