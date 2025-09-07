package taskmanagement.ui.adapters;

import taskmanagement.application.viewmodel.ExportFormat;
import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.application.viewmodel.sort.SortStrategy;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.ui.api.TasksViewAPI;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapter that bridges the UI-facing {@link TasksViewAPI} to the {@link TasksViewModel}.
 * <p>
 * Exposes observable properties and delegates UI commands to the ViewModel while
 * preserving UI-friendly types. Centralizes exception handling so UI code remains simple.
 */
public final class TasksViewApiAdapter implements TasksViewAPI {

    private final TasksViewModel vm;
    private final RowsPropertyAdapter rowsAdapter;
    private final RowsPropertyAdapter filteredRowsAdapter;

    /**
     * Creates a new adapter around the given ViewModel.
     *
     * @param vm the ViewModel instance; must not be {@code null}
     * @throws NullPointerException if {@code vm} is {@code null}
     */
    public TasksViewApiAdapter(TasksViewModel vm) {
        this.vm = Objects.requireNonNull(vm, "vm");
        this.rowsAdapter = new RowsPropertyAdapter(vm.rowsProperty());
        this.filteredRowsAdapter = new RowsPropertyAdapter(vm.filteredRowsProperty());
    }

    /**
     * Returns an observable list of UI-facing tasks.
     *
     * @return the property containing {@code List<ITask>}
     */
    @Override
    public Property<List<ITask>> tasksProperty() {
        return rowsAdapter.asProperty();
    }

    /**
     * Returns an observable list of UI-facing tasks after filtering.
     *
     * @return the property containing filtered {@code List<ITask>}
     */
    @Override
    public Property<List<ITask>> filteredTasksProperty() {
        return filteredRowsAdapter.asProperty();
    }

    /**
     * Indicates whether an undo operation is currently available.
     *
     * @return property reflecting undo availability
     */
    @Override
    public Property<Boolean> canUndoProperty() {
        return vm.canUndoProperty();
    }

    /**
     * Indicates whether a redo operation is currently available.
     *
     * @return property reflecting redo availability
     */
    @Override
    public Property<Boolean> canRedoProperty() {
        return vm.canRedoProperty();
    }

    /**
     * Reloads tasks from the underlying data source.
     *
     * @return {@code null} (for fluent API compatibility)
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void reload() {
        return safeVm(() -> { vm.reload(); return null; }, "reload");
    }

    /**
     * Deletes all tasks.
     *
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void deleteAll() {
        return safeVm(() -> { vm.deleteAll(); return null; }, "deleteAll");
    }

    /**
     * Deletes the tasks with the provided identifiers.
     *
     * @param ids task identifiers
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void deleteTasks(int... ids) {
        return safeVm(() -> { vm.deleteTasks(ids); return null; }, "deleteTasks");
    }

    /**
     * Advances the lifecycle state of a task by its identifier.
     *
     * @param id task identifier
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void advanceState(int id) {
        return safeVm(() -> { vm.advanceState(id); return null; }, "advanceState");
    }

    /**
     * Marks a task with a specific state.
     *
     * @param id    task identifier
     * @param state new state to set
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void markState(int id, TaskState state) {
        return safeVm(() -> { vm.markState(id, state); return null; }, "markState");
    }

    /**
     * Adds a new task using values from a UI-level {@link ITask}.
     *
     * @param task UI task whose title, description, and state are used
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void addTask(ITask task) {
        return safeVm(() -> {
            vm.addTask(task.getTitle(), task.getDescription(), task.getState());
            return null;
        }, "addTask");
    }

    /**
     * Updates an existing task using values from a UI-level {@link ITask}.
     *
     * @param task UI task providing id, title, description, and state
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void updateTask(ITask task) {
        return safeVm(() -> {
            vm.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getState());
            vm.reload();
            return null;
        }, "updateTask");
    }

    /**
     * Performs an undo operation if available.
     *
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void undo() {
        return safeVm(() -> { vm.undo(); return null; }, "undo");
    }

    /**
     * Performs a redo operation if available.
     *
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void redo() {
        return safeVm(() -> { vm.redo(); return null; }, "redo");
    }

    /**
     * Applies a task filter.
     *
     * @param filter the filter to apply
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void setFilter(ITaskFilter filter) {
        return safeVm(() -> { vm.setFilter(filter); return null; }, "setFilter");
    }

    /**
     * Clears the active task filter.
     *
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void clearFilter() {
        return safeVm(() -> { vm.clearFilter(); return null; }, "clearFilter");
    }

    /**
     * Sets the sorting strategy for tasks.
     *
     * @param strategy the sorting strategy to use
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void setSortStrategy(SortStrategy strategy) {
        return safeVm(() -> { vm.setSortStrategy(strategy); return null; }, "setSortStrategy");
    }

    /**
     * Computes counts of tasks by state.
     *
     * @param useFiltered whether to use the filtered list
     * @return a {@link ByStateCount} report
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public ByStateCount getCountsByState(boolean useFiltered) {
        return safeVm(() -> vm.getCountsByState(useFiltered), "getCountsByState");
    }

    /**
     * Exports tasks to a file in the selected format.
     *
     * @param path        output file path
     * @param format      export format
     * @param useFiltered whether to use the filtered list
     * @param ids         optional subset of task ids to export
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void exportTasks(Path path, ExportFormat format, boolean useFiltered, List<Integer> ids) {
        return safeVm(() -> { vm.exportTasks(path, format, useFiltered, ids); return null; }, "exportTasks");
    }

    /**
     * Exports a by-state report to a file in the selected format.
     *
     * @param path        output file path
     * @param format      export format
     * @param useFiltered whether to use the filtered list
     * @param ids         optional subset of task ids to include
     * @return {@code null}
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Void exportByStateReport(Path path, ExportFormat format, boolean useFiltered, List<Integer> ids) {
        return safeVm(() -> { vm.exportByStateReport(path, format, useFiltered, ids); return null; }, "exportByStateReport");
    }

    /**
     * Finds a view-model row by its identifier.
     *
     * @param id task identifier
     * @return an {@link Optional} containing the matching row, if found
     * @throws IllegalStateException if the ViewModel operation fails
     */
    @Override
    public Optional<TasksViewModel.RowDTO> findRowById(int id) {
        return safeVm(() -> vm.findRowById(id), "findRowById");
    }

    /**
     * Executes a ViewModel operation and wraps any thrown exception in an {@link IllegalStateException}.
     *
     * @param action operation to execute
     * @param opName operation name for diagnostics
     * @param <T>    return type
     * @return the operation result
     * @throws IllegalStateException if {@code action} throws an exception
     */
    private static <T> T safeVm(CheckedSupplier<T> action, String opName) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new IllegalStateException("ViewModel operation failed: " + opName, e);
        }
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
