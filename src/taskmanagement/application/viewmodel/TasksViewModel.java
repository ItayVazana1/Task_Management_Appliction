package taskmanagement.application.viewmodel;


import taskmanagement.application.viewmodel.commands.AddTaskCommand;
import taskmanagement.application.viewmodel.commands.CommandException;
import taskmanagement.application.viewmodel.commands.CommandStack;
import taskmanagement.application.viewmodel.commands.DeleteTaskCommand;
import taskmanagement.application.viewmodel.commands.MarkStateCommand;
import taskmanagement.application.viewmodel.commands.UpdateTaskCommand;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.application.viewmodel.sort.SortStrategy;
import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.domain.visitor.TaskVisitor;
import taskmanagement.domain.visitor.adapters.ByStateCsvExporter;
import taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter;
import taskmanagement.domain.visitor.adapters.IReportExporter;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.domain.visitor.export.CsvFlatTaskVisitor;
import taskmanagement.domain.visitor.export.PlainTextFlatTaskVisitor;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * ViewModel layer (MVVM) for managing tasks.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Mediates between the Swing UI and the DAO-based model.</li>
 *   <li>Exposes immutable UI-friendly row DTOs.</li>
 *   <li>Provides CRUD and state-changing operations via the Command pattern with undo/redo.</li>
 *   <li>Supports filtering via Combinator and reporting/export via Visitor (records & pattern matching).</li>
 *   <li>Notifies the UI through observable properties (Observer pattern).</li>
 * </ul>
 */
public final class TasksViewModel {

    private final ITasksDAO dao;
    private final CommandStack commands = new CommandStack();
    private List<ITask> snapshot = Collections.emptyList();
    private volatile Optional<ITaskFilter> activeFilter = Optional.empty();
    private volatile Optional<SortStrategy> currentSort = Optional.empty();

    private final Property<List<RowDTO>> rowsProperty = new Property<>(List.of());
    private final Property<List<RowDTO>> filteredRowsProperty = new Property<>(List.of());
    private final Property<Boolean> canUndoProperty = new Property<>(false);
    private final Property<Boolean> canRedoProperty = new Property<>(false);

    /**
     * Immutable UI row data transfer object.
     *
     * @param id          task id
     * @param title       task title
     * @param description task description
     * @param state       task state as string
     */
    public record RowDTO(int id, String title, String description, String state) { }

    /**
     * Constructs a new {@code TasksViewModel}.
     *
     * @param dao the tasks DAO (must not be {@code null})
     * @throws NullPointerException if {@code dao} is {@code null}
     */
    public TasksViewModel(ITasksDAO dao) {
        this.dao = Objects.requireNonNull(dao, "ITasksDAO must not be null");
        updateCommandAvailability();
    }

    // --- Observable properties (for UI binding) ---

    /** Property that emits unfiltered rows whenever data changes. */
    public Property<List<RowDTO>> rowsProperty() { return rowsProperty; }
    /** Property that emits filtered rows whenever data or filter changes. */
    public Property<List<RowDTO>> filteredRowsProperty() { return filteredRowsProperty; }
    /** Property that emits {@code true} when undo is available. */
    public Property<Boolean> canUndoProperty() { return canUndoProperty; }
    /** Property that emits {@code true} when redo is available. */
    public Property<Boolean> canRedoProperty() { return canRedoProperty; }

    // --- Sorting API ---

    /** Sets the active sorting strategy. Passing {@code null} clears sorting. */
    public void setSortStrategy(SortStrategy strategy) {
        this.currentSort = Optional.ofNullable(strategy);
        rowsProperty.setValue(buildAllRowsSorted());
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
    }

    /** Clears any active sort strategy and reverts to DAO order. */
    public void clearSortStrategy() { setSortStrategy(null); }

    // --- Data reload and queries ---

    /**
     * Reloads tasks from the DAO into an internal snapshot
     * and updates observable properties.
     *
     * @throws TasksDAOException if DAO retrieval fails
     */
    public void reload() throws TasksDAOException {
        ITask[] arr = dao.getTasks();
        this.snapshot = (arr == null) ? List.of() : Arrays.asList(arr);

        rowsProperty.setValue(buildAllRowsSorted());
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
        updateCommandAvailability();
    }

    /** Returns a UI-friendly immutable snapshot of all tasks. */
    public List<RowDTO> getRows() {
        List<RowDTO> out = new ArrayList<>(snapshot.size());
        for (ITask t : snapshot) {
            out.add(new RowDTO(
                    t.getId(),
                    safe(t.getTitle()),
                    safe(t.getDescription()),
                    (t.getState() == null) ? "" : t.getState().name()
            ));
        }
        return Collections.unmodifiableList(out);
    }

    /** Finds a row by its id. */
    public Optional<RowDTO> findRowById(int id) {
        validateId(id);
        for (ITask t : snapshot) if (t.getId() == id) return Optional.of(toRowDTO(t));
        return Optional.empty();
    }

    /** Returns available task states for UI binding. */
    public TaskState[] getAvailableStates() { return TaskState.values(); }

    // --- Filtering API (Combinator) ---

    /** Sets the active filter and updates filtered rows. */
    public void setFilter(ITaskFilter filter) {
        this.activeFilter = Optional.of(Objects.requireNonNull(filter, "filter must not be null"));
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
    }

    /** Clears the active filter and updates filtered rows. */
    public void clearFilter() {
        this.activeFilter = Optional.empty();
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
    }

    /** Returns a snapshot of rows filtered with the active filter (if any). */
    public List<RowDTO> getFilteredRows() {
        final List<RowDTO> out = new ArrayList<>();
        final var opt = this.activeFilter;

        if (opt.isEmpty()) {
            for (ITask t : snapshot) out.add(toRowDTO(t));
        } else {
            final ITaskFilter f = opt.get();
            for (ITask t : snapshot) if (f.test(t)) out.add(toRowDTO(t));
        }
        return Collections.unmodifiableList(out);
    }

    // --- Command operations (CRUD + State) ---

    /** Adds a new task via {@link AddTaskCommand}. */
    public void addTask(String title, String description, TaskState state) throws CommandException {
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        Task toAdd = new Task(0, title, description, state);
        commands.execute(new AddTaskCommand(dao, toAdd));
        refreshAfterMutation();
    }

    /** Updates an existing task. */
    public void updateTask(int id, String title, String description, TaskState state) throws CommandException {
        validateId(id);
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        final ITask before;
        try {
            before = dao.getTask(id);
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to load task for update: id=" + id, e);
        }
        if (before == null) throw new CommandException("Task not found for update: id=" + id);

        Task after = new Task(id, title, description, state);
        commands.execute(new UpdateTaskCommand(dao, before, after));
        refreshAfterMutation();
    }

    /** Deletes a task. */
    public void deleteTask(int id) throws CommandException {
        validateId(id);
        final ITask snapshotToDelete;
        try {
            snapshotToDelete = dao.getTask(id);
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to load task for delete: id=" + id, e);
        }
        if (snapshotToDelete == null) throw new CommandException("Task not found for delete: id=" + id);
        commands.execute(new DeleteTaskCommand(dao, snapshotToDelete));
        refreshAfterMutation();
    }

    /** Deletes multiple tasks. */
    public void deleteTasks(Collection<Integer> ids) throws CommandException {
        if (ids == null || ids.isEmpty()) return;
        boolean changed = false;

        for (Integer id : ids) {
            if (id == null) continue;
            validateId(id);
            final ITask t;
            try {
                t = dao.getTask(id);
            } catch (TasksDAOException e) {
                if (isNotFound(e)) continue;
                throw new CommandException("Failed to load task for delete: id=" + id, e);
            }
            if (t != null) {
                commands.execute(new DeleteTaskCommand(dao, t));
                changed = true;
            }
        }
        if (changed) refreshAfterMutation();
    }

    /** Deletes multiple tasks (varargs). */
    public void deleteTasks(int... ids) throws CommandException {
        if (ids == null || ids.length == 0) return;
        List<Integer> list = new ArrayList<>(ids.length);
        for (int id : ids) list.add(id);
        deleteTasks(list);
    }

    /** Deletes all tasks. */
    public void deleteAll() throws TasksDAOException {
        dao.deleteTasks();
        reload();
    }

    /** Marks a task with an explicit state. */
    public void markState(int id, TaskState state) throws CommandException {
        transitionState(id, state);
    }

    /**
     * Transitions a task to a new state if allowed.
     *
     * @param id     task id
     * @param target target state
     * @throws CommandException if task not found, illegal transition, or DAO fails
     */
    public void transitionState(int id, TaskState target) throws CommandException {
        validateId(id);
        Objects.requireNonNull(target, "target state must not be null");

        final ITask before;
        try {
            before = dao.getTask(id);
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to load task for transition: id=" + id, e);
        }
        if (before == null) throw new CommandException("Task not found for transition: id=" + id);

        final TaskState current = before.getState();
        if (current == null) throw new CommandException("Task has no current state: id=" + id);

        int curIdx = stateIndex(current);
        int tgtIdx = stateIndex(target);
        if (tgtIdx < curIdx) {
            throw new CommandException("Backward transition not allowed: " + current + " -> " + target);
        }
        if (tgtIdx == curIdx) {
            return; // no-op
        }

        // MarkStateCommand expects (dao, before, targetState, TaskFactory)
        MarkStateCommand.TaskFactory factory = (prev, state) ->
                new Task(prev.getId(), prev.getTitle(), prev.getDescription(), state);

        commands.execute(new MarkStateCommand(dao, before, target, factory));
        refreshAfterMutation();
    }

    /**
     * Advances a task to its next legal state.
     *
     * @param id task id
     * @throws CommandException if task not found or DAO fails
     */
    public void advanceState(int id) throws CommandException {
        validateId(id);
        final ITask before;
        try {
            before = dao.getTask(id);
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to load task for advance: id=" + id, e);
        }
        if (before == null) throw new CommandException("Task not found for advance: id=" + id);

        TaskState cur = before.getState();
        if (cur == null) throw new CommandException("Task has no current state: id=" + id);

        final TaskState next = switch (cur) {
            case ToDo       -> TaskState.InProgress;
            case InProgress -> TaskState.Completed;
            case Completed  -> null;
        };
        if (next == null) return; // already terminal

        MarkStateCommand.TaskFactory factory = (prev, state) ->
                new Task(prev.getId(), prev.getTitle(), prev.getDescription(), state);

        commands.execute(new MarkStateCommand(dao, before, next, factory));
        refreshAfterMutation();
    }


    // --- Undo/Redo ---

    /** @return true if undo is possible */
    public boolean canUndo() { return commands.canUndo(); }
    /** @return true if redo is possible */
    public boolean canRedo() { return commands.canRedo(); }

    /** Undoes the last executed command. */
    public void undo() throws CommandException {
        commands.undo();
        refreshAfterMutation();
    }

    /** Redoes the last undone command. */
    public void redo() throws CommandException {
        commands.redo();
        refreshAfterMutation();
    }

    // --- Reporting & Export ---

    /**
     * Returns a report of task counts by state.
     *
     * @param useFiltered true to apply filter
     * @return counts by state
     */
    public ByStateCount getCountsByState(boolean useFiltered) {
        final ByStateCount out = new ByStateCount();
        final List<ITask> base = useFiltered ? extractFilteredTasks() : (snapshot == null ? List.of() : snapshot);
        for (ITask t : base) {
            if (t == null || t.getState() == null) continue;
            out.inc(t.getState());
        }
        return out;
    }

    /**
     * Returns counts as a map by state.
     *
     * @param useFiltered true to apply filter
     * @return map of counts
     */
    public EnumMap<TaskState, Integer> getCountsMapByState(boolean useFiltered) {
        final ByStateCount c = getCountsByState(useFiltered);
        final EnumMap<TaskState, Integer> map = new EnumMap<>(TaskState.class);
        map.put(TaskState.ToDo,       c.todo());
        map.put(TaskState.InProgress, c.inProgress());
        map.put(TaskState.Completed,  c.completed());
        return map;
    }

    /**
     * Exports tasks to a file (flat list).
     *
     * @param path        file path
     * @param format      export format
     * @param useFiltered whether to apply filter
     * @throws IOException       if IO fails
     * @throws TasksDAOException if DAO fails
     */
    public void exportTasks(Path path, ExportFormat format, boolean useFiltered)
            throws IOException, TasksDAOException {
        exportTasks(path, format, useFiltered, /*filteredIds*/ null);
    }

    /**
     * Exports tasks with optional filtered IDs (flat list).
     *
     * @param path        file path
     * @param format      export format
     * @param useFiltered whether to apply filter
     * @param filteredIds optional filtered ids (export only these if provided)
     * @throws IOException       if IO fails
     * @throws TasksDAOException if DAO fails
     */
    public void exportTasks(Path path, ExportFormat format, boolean useFiltered, List<Integer> filteredIds)
            throws IOException, TasksDAOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(format, "format");

        final List<ITask> tasks = loadTasksForExport(useFiltered, filteredIds);

        switch (format) {
            case CSV -> {
                var visitor = new CsvFlatTaskVisitor();
                for (ITask t : tasks) {
                    if (t == null || t.getState() == null) continue;
                    String title = safe(t.getTitle());
                    String desc  = safe(t.getDescription());
                    switch (t.getState()) {
                        case ToDo       -> visitor.visit(new ToDoTaskRec(t.getId(), title, desc));
                        case InProgress -> visitor.visit(new InProgressTaskRec(t.getId(), title, desc));
                        case Completed  -> visitor.visit(new CompletedTaskRec(t.getId(), title, desc));
                    }
                }
                visitor.complete();
                String content = visitor.result();
                if (path.getParent() != null) Files.createDirectories(path.getParent());
                Files.writeString(path, content, StandardCharsets.UTF_8);
            }
            case TXT -> {
                var visitor = new PlainTextFlatTaskVisitor();
                for (ITask t : tasks) {
                    if (t == null || t.getState() == null) continue;
                    String title = safe(t.getTitle());
                    String desc  = safe(t.getDescription());
                    switch (t.getState()) {
                        case ToDo       -> visitor.visit(new ToDoTaskRec(t.getId(), title, desc));
                        case InProgress -> visitor.visit(new InProgressTaskRec(t.getId(), title, desc));
                        case Completed  -> visitor.visit(new CompletedTaskRec(t.getId(), title, desc));
                    }
                }
                visitor.complete();
                String content = visitor.result();
                if (path.getParent() != null) Files.createDirectories(path.getParent());
                Files.writeString(path, content, StandardCharsets.UTF_8);
            }
        }
    }



    /**
     * Exports a by-state report (aggregated counts).
     *
     * @param path        file path
     * @param format      export format
     * @param useFiltered whether to apply filter
     * @param filteredIds optional ids (ignored for aggregated unless you want to restrict the base set)
     * @throws IOException       if IO fails
     * @throws TasksDAOException if DAO fails
     */
    public void exportByStateReport(Path path, ExportFormat format, boolean useFiltered, List<Integer> filteredIds)
            throws IOException, TasksDAOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(format, "format");

        // Count
        final List<ITask> base = loadTasksForExport(useFiltered, filteredIds);
        final ByStateCount counts = new ByStateCount();
        for (ITask t : base) {
            if (t == null || t.getState() == null) continue;
            counts.inc(t.getState());
        }

        // Your IReportExporter<T>.export(T) takes only the report; we'll write the returned content to file
        final IReportExporter<ByStateCount> exporter = switch (format) {
            case CSV -> new ByStateCsvExporter();
            case TXT -> new ByStatePlainTextExporter();
        };

        final String content = exporter.export(counts); // <-- no Path here
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }


    // --- Helpers ---

    private void refreshAfterMutation() throws CommandException {
        try {
            reload();
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to refresh after mutation", e);
        }
    }

    private void updateCommandAvailability() {
        canUndoProperty.setValue(commands.canUndo());
        canRedoProperty.setValue(commands.canRedo());
    }

    private List<ITask> loadTasksForExport(boolean useFiltered, List<Integer> filteredIds) throws TasksDAOException {
        if (useFiltered && filteredIds != null && !filteredIds.isEmpty()) {
            final List<ITask> out = new ArrayList<>(filteredIds.size());
            for (Integer id : filteredIds) {
                if (id == null) continue;
                final ITask t = dao.getTask(id);
                if (t != null) out.add(t);
            }
            return out;
        }
        final ITask[] arr = dao.getTasks();
        return (arr == null) ? List.of() : Arrays.asList(arr);
    }

    private List<ITask> extractFilteredTasks() {
        if (activeFilter.isEmpty()) return (snapshot == null) ? List.of() : snapshot;
        final ITaskFilter f = activeFilter.get();
        final List<ITask> out = new ArrayList<>();
        for (ITask t : snapshot) if (f.test(t)) out.add(t);
        return out;
    }

    private static String safe(String s) { return (s == null) ? "" : s; }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
    }

    private static void validateId(int id) {
        if (id < 0) throw new IllegalArgumentException("invalid id: " + id);
    }

    private static RowDTO toRowDTO(ITask t) {
        return new RowDTO(
                t.getId(),
                safe(t.getTitle()),
                safe(t.getDescription()),
                (t.getState() == null) ? "" : t.getState().name()
        );
    }

    private List<RowDTO> buildAllRowsSorted() {
        final List<ITask> tasks = sortedSnapshot();
        final List<RowDTO> out = new ArrayList<>(tasks.size());
        for (ITask t : tasks) out.add(toRowDTO(t));
        return Collections.unmodifiableList(out);
    }

    private List<RowDTO> buildFilteredRowsSorted() {
        final List<ITask> filtered = extractFilteredTasks();
        final List<ITask> ordered = currentSort.map(s -> s.sort(filtered)).orElse(filtered);
        final List<RowDTO> out = new ArrayList<>(ordered.size());
        for (ITask t : ordered) out.add(toRowDTO(t));
        return Collections.unmodifiableList(out);
    }

    private List<ITask> sortedSnapshot() {
        final List<ITask> base = (snapshot == null) ? List.of() : snapshot;
        return currentSort.map(s -> s.sort(base)).orElse(base);
    }

    private static boolean isNotFound(TasksDAOException e) {
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase(Locale.ROOT).contains("not found");
    }

    private static int stateIndex(TaskState s) {
        return switch (s) {
            case ToDo -> 0;
            case InProgress -> 1;
            case Completed -> 2;
        };
    }
}
