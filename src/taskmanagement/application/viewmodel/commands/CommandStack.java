package taskmanagement.application.viewmodel.commands;

import java.util.Stack;

/**
 * Maintains a stack of executed commands and supports undo/redo operations.
 * Implements the Command design pattern.
 */
public class CommandStack {

    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    /**
     * Execute a command and push it onto the undo stack.
     *
     * @param cmd the command to execute
     * @throws Exception if command execution fails
     */
    public void perform(Command cmd) throws Exception {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    /**
     * Undo the most recent command.
     *
     * @throws Exception if undo fails
     */
    public void undo() throws Exception {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    /**
     * Redo the most recently undone command.
     *
     * @throws Exception if redo fails
     */
    public void redo() throws Exception {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }

    /**
     * Redo all commands currently in the redo stack.
     * Useful when the user wants to "restore to tip".
     *
     * @throws Exception if any redo step fails
     */
    public void redoAll() throws Exception {
        while (!redoStack.isEmpty()) {
            redo();
        }
    }

    /** @return true if there is at least one command to undo */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /** @return true if there is at least one command to redo */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
