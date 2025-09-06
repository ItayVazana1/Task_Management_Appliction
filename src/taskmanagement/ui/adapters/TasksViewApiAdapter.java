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
 * TasksViewApiAdapter
 * -------------------
 * Adapter from the UI-facing {@link TasksViewAPI} to the {@link TasksViewModel}.
 *
 * Responsibilities:
 *  • Expose observable properties to the UI (no domain leakage).
 *  • Delegate UI commands to the ViewModel, converting signatures as needed.
 *  • Centralize exception handling so UI code can stay simple.
 *
 * Note: The adapter never touches the DAO directly and contains no domain logic.
 */
public final class TasksViewApiAdapter implements TasksViewAPI {

    /** Backing ViewModel (MVVM). */
    private final TasksViewModel vm;

    /** Bridges RowDTO<List> → ITask<List> for UI-friendly binding. */
    private final RowsPropertyAdapter rowsAdapter;

    /** Bridges filtered RowDTO<List> → ITask<List> for UI-friendly binding. */
    private final RowsPropertyAdapter filteredRowsAdapter;

    /**
     * Creates a new adapter around the given ViewModel.
     *
     * @param vm non-null ViewModel instance
     * @throws NullPointerException if {@code vm} is null
     */
    public TasksViewApiAdapter(TasksViewModel vm) {
        this.vm = Objects.requireNonNull(vm, "vm");
        this.rowsAdapter = new RowsPropertyAdapter(vm.rowsProperty());
        this.filteredRowsAdapter = new RowsPropertyAdapter(vm.filteredRowsProperty());
    }

    // ---------------------------------------------------------------------
    // Properties
    // ---------------------------------------------------------------------

    @Override
    public Property<List<ITask>> tasksProperty() {
        return rowsAdapter.asProperty();
    }

    @Override
    public Property<List<ITask>> filteredTasksProperty() {
        return filteredRowsAdapter.asProperty();
    }

    @Override
    public Property<Boolean> canUndoProperty() {
        return vm.canUndoProperty();
    }

    @Override
    public Property<Boolean> canRedoProperty() {
        return vm.canRedoProperty();
    }

    // ---------------------------------------------------------------------
    // Core Operations
    // ---------------------------------------------------------------------

    @Override
    public Void reload() {
        return safeVm(() -> { vm.reload(); return null; }, "reload");
    }

    @Override
    public Void deleteAll() {
        return safeVm(() -> { vm.deleteAll(); return null; }, "deleteAll");
    }

    @Override
    public Void deleteTasks(int... ids) {
        return safeVm(() -> { vm.deleteTasks(ids); return null; }, "deleteTasks");
    }

    @Override
    public Void advanceState(int id) {
        return safeVm(() -> { vm.advanceState(id); return null; }, "advanceState");
    }

    @Override
    public Void markState(int id, TaskState state) {
        return safeVm(() -> { vm.markState(id, state); return null; }, "markState");
    }

    @Override
    public Void addTask(ITask task) {
        return safeVm(() -> {
            vm.addTask(task.getTitle(), task.getDescription(), task.getState());
            return null;
        }, "addTask");
    }

    /**
     * Bridges the UI-level {@code ITask} proxy into the ViewModel's update signature.
     */
    @Override
    public Void updateTask(ITask task) {
        return safeVm(() -> {
            vm.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getState());
            vm.reload(); // keep behavior you already had
            return null;
        }, "updateTask");
    }

    @Override
    public Void undo() {
        return safeVm(() -> { vm.undo(); return null; }, "undo");
    }

    @Override
    public Void redo() {
        return safeVm(() -> { vm.redo(); return null; }, "redo");
    }

    // ---------------------------------------------------------------------
    // Filtering / Sorting
    // ---------------------------------------------------------------------

    @Override
    public Void setFilter(ITaskFilter filter) {
        return safeVm(() -> { vm.setFilter(filter); return null; }, "setFilter");
    }

    @Override
    public Void clearFilter() {
        return safeVm(() -> { vm.clearFilter(); return null; }, "clearFilter");
    }

    @Override
    public Void setSortStrategy(SortStrategy strategy) {
        return safeVm(() -> { vm.setSortStrategy(strategy); return null; }, "setSortStrategy");
    }

    // ---------------------------------------------------------------------
    // Reporting
    // ---------------------------------------------------------------------

    @Override
    public ByStateCount getCountsByState(boolean useFiltered) {
        return safeVm(() -> vm.getCountsByState(useFiltered), "getCountsByState");
    }

    @Override
    public Void exportTasks(Path path, ExportFormat format, boolean useFiltered, List<Integer> ids) {
        return safeVm(() -> { vm.exportTasks(path, format, useFiltered, ids); return null; }, "exportTasks");
    }

    @Override
    public Void exportByStateReport(Path path, ExportFormat format, boolean useFiltered, List<Integer> ids) {
        return safeVm(() -> { vm.exportByStateReport(path, format, useFiltered, ids); return null; }, "exportByStateReport");
    }

    // ---------------------------------------------------------------------
    // Lookup for dialogs
    // ---------------------------------------------------------------------

    @Override
    public Optional<TasksViewModel.RowDTO> findRowById(int id) {
        return safeVm(() -> vm.findRowById(id), "findRowById");
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

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
