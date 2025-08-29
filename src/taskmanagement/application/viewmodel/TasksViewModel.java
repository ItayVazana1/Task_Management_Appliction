package taskmanagement.application.viewmodel;

import taskmanagement.application.viewmodel.commands.*;
import taskmanagement.application.viewmodel.events.ObservableList;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.application.viewmodel.sort.SortByTitle;
import taskmanagement.application.viewmodel.sort.SortStrategy;
import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.Arrays;
import java.util.List;

/**
 * ViewModel for tasks. Exposes observable state for the Swing view.
 * MVVM note: no UI types here; the View listens via Property/ObservableList.
 */
public final class TasksViewModel {

    private final ObservableList<ITask> tasks = new ObservableList<>();
    private final Property<ITask> selectedTask = new Property<>(null);
    private final Property<SortStrategy> sortStrategy = new Property<>(new SortByTitle());
    private final CommandStack commandStack = new CommandStack();

    /**
     * Loads all tasks from the DAO and publishes a sorted snapshot.
     */
    public void loadAll() throws TasksDAOException {
        ITasksDAO dao = DAOProvider.get();
        ITask[] arr = dao.getTasks();
        publishSorted(Arrays.asList(arr));
    }

    /**
     * Adds a new task via command stack (supports undo/redo).
     */
    public void addTask(ITask task) throws Exception {
        commandStack.perform(new AddTaskCommand(task));
        loadAll();
    }

    /**
     * Updates a task via command stack (supports undo/redo).
     */
    public void updateTask(ITask before, ITask after) throws Exception {
        commandStack.perform(new UpdateTaskCommand(before, after));
        loadAll();
    }

    /**
     * Deletes a task via command stack (supports undo/redo).
     */
    public void deleteTask(ITask snapshot) throws Exception {
        commandStack.perform(new DeleteTaskCommand(snapshot));
        loadAll();
    }

    /**
     * Marks task state via command stack (supports undo/redo).
     */
    public void markState(ITask before, TaskState target) throws Exception {
        MarkStateCommand.TaskFactory factory = (src, st) ->
                new Task(src.getId(), src.getTitle(), src.getDescription(), st);
        commandStack.perform(new MarkStateCommand(before, target, factory));
        loadAll();
    }

    /**
     * Sets the current sorting strategy and republishes the view.
     */
    public void setSortStrategy(SortStrategy strategy) throws TasksDAOException {
        sortStrategy.set(strategy);
        loadAll();
    }

    /** Undoes last command if available. */
    public void undo() throws Exception {
        commandStack.undo();
        loadAll();
    }

    /** Redoes last command if available. */
    public void redo() throws Exception {
        commandStack.redo();
        loadAll();
    }

    /** @return observable list of tasks (view listens for changes). */
    public ObservableList<ITask> tasks() { return tasks; }

    /** @return selected task property for UI binding. */
    public Property<ITask> selectedTask() { return selectedTask; }

    /** @return current sorting strategy property for UI binding. */
    public Property<SortStrategy> sortStrategy() { return sortStrategy; }

    /** @return true if an undo operation is available. */
    public boolean canUndo() { return commandStack.canUndo(); }

    /** @return true if a redo operation is available. */
    public boolean canRedo() { return commandStack.canRedo(); }

    // ---------- internal helpers ----------

    private void publishSorted(List<ITask> raw) {
        tasks.clear();
        SortStrategy s = sortStrategy.get();
        List<ITask> view = (s == null) ? raw : s.sort(raw);
        for (ITask t : view) {
            tasks.add(t);
        }
    }
}