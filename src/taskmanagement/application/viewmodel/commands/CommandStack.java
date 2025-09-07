package taskmanagement.application.viewmodel.commands;

import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.TasksDAOException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Maintains undo and redo stacks for executed commands.
 * <p>
 * This class implements the Command design pattern infrastructure,
 * allowing commands to be executed, undone, and redone.
 * It provides both:
 * <ul>
 *   <li>Pull-style status checks via {@link #canUndo()} and {@link #canRedo()}.</li>
 *   <li>Push-style observables for UI binding via {@link #canUndoProperty()} and {@link #canRedoProperty()}.</li>
 * </ul>
 * </p>
 */
public final class CommandStack {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    private final Property<Boolean> canUndoProp = new Property<>(false);
    private final Property<Boolean> canRedoProp = new Property<>(false);

    /**
     * Executes the given command, pushes it onto the undo stack,
     * and clears the redo stack.
     *
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
     * Undoes the last executed command and moves it to the redo stack.
     * Does nothing if there is no command to undo.
     *
     * @throws CommandException if undo fails
     */
    public void undo() throws CommandException {
        if (undoStack.isEmpty()) {
            return;
        }
        Command c = undoStack.pop();
        try {
            c.undo();
            redoStack.push(c);
            refreshFlags();
        } catch (TasksDAOException | ValidationException ex) {
            undoStack.push(c);
            refreshFlags();
            throw new CommandException("Failed to undo command: " + c.name(), ex);
        }
    }

    /**
     * Redoes the last undone command and moves it back to the undo stack.
     * Does nothing if there is no command to redo.
     *
     * @throws CommandException if redo fails
     */
    public void redo() throws CommandException {
        if (redoStack.isEmpty()) {
            return;
        }
        Command c = redoStack.pop();
        try {
            c.execute();
            undoStack.push(c);
            refreshFlags();
        } catch (TasksDAOException | ValidationException ex) {
            redoStack.push(c);
            refreshFlags();
            throw new CommandException("Failed to redo command: " + c.name(), ex);
        }
    }

    /**
     * Indicates whether an undo operation is available.
     *
     * @return {@code true} if there is at least one command to undo
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Indicates whether a redo operation is available.
     *
     * @return {@code true} if there is at least one command to redo
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Provides an observable property for the undo availability flag.
     * Useful for enabling or disabling undo-related UI controls.
     *
     * @return the observable undo property
     */
    public Property<Boolean> canUndoProperty() {
        return canUndoProp;
    }

    /**
     * Provides an observable property for the redo availability flag.
     * Useful for enabling or disabling redo-related UI controls.
     *
     * @return the observable redo property
     */
    public Property<Boolean> canRedoProperty() {
        return canRedoProp;
    }

    /**
     * Clears both the undo and redo stacks and refreshes availability flags.
     * Typically used when resetting the application state.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        refreshFlags();
    }

    /**
     * Returns the number of commands currently available for undo.
     *
     * @return the size of the undo stack
     */
    public int undoCount() {
        return undoStack.size();
    }

    /**
     * Returns the number of commands currently available for redo.
     *
     * @return the size of the redo stack
     */
    public int redoCount() {
        return redoStack.size();
    }

    /**
     * Updates the observable flags for undo and redo availability.
     */
    private void refreshFlags() {
        canUndoProp.setValue(!undoStack.isEmpty());
        canRedoProp.setValue(!redoStack.isEmpty());
    }
}
