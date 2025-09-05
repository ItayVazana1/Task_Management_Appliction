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
import taskmanagement.domain.visitor.CountByStateVisitor;
import taskmanagement.domain.visitor.TaskVisitor;
import taskmanagement.domain.visitor.adapters.ByStateCsvExporter;
import taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter;
import taskmanagement.domain.visitor.adapters.IReportExporter;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.*;

/**
 * ViewModel (MVVM) for the Tasks screen.
 *
 * <p><strong>Responsibilities</strong>:
 * <ul>
 *   <li>Mediates between Swing UI and the DAO-backed model.</li>
 *   <li>Exposes UI-friendly read snapshots (no domain leakage).</li>
 *   <li>Provides imperative commands for CRUD and state changes (Command + Undo/Redo).</li>
 *   <li>Exports & reports via Visitor + Records & Pattern Matching.</li>
 *   <li>Notifies UI (Observer) when rows change.</li>
 * </ul>
 *
 * <p><strong>Notes</strong>:
 * <ul>
 *   <li>UI must not touch {@link ITask} / {@link Task} directly.</li>
 *   <li>After each mutation/undo/redo, the ViewModel reloads and fires properties.</li>
 * </ul>
 */
public final class TasksViewModel {

    // =====================
    // Dependencies & State
    // =====================

    /** DAO dependency (model). */
    private final ITasksDAO dao;

    /** Command stack for undo/redo (Command pattern). */
    private final CommandStack commands = new CommandStack();

    /** Cached snapshot of tasks for UI consumption (latest reload). */
    private List<ITask> snapshot = Collections.emptyList();

    /** Optional active filter built with Combinator logic (domain.filter). */
    private volatile Optional<ITaskFilter> activeFilter = Optional.empty();

    /** Optional sorting strategy. When empty, keep DAO order (insertion/id). */
    private volatile Optional<SortStrategy> currentSort = Optional.empty();

    // ---- Observer: observable properties (UI binds to these) ----
    /** Unfiltered rows for UI binding (immutable list). */
    private final Property<List<RowDTO>> rowsProperty = new Property<>(List.of());
    /** Filtered rows for UI binding (immutable list). */
    private final Property<List<RowDTO>> filteredRowsProperty = new Property<>(List.of());
    /** Command availability for enabling/disabling buttons in UI. */
    private final Property<Boolean> canUndoProperty = new Property<>(false);
    private final Property<Boolean> canRedoProperty = new Property<>(false);

    // =====================
    // Types
    // =====================

    /**
     * Immutable UI row DTO (decouples UI from domain types).
     *
     * @param id          task id
     * @param title       title
     * @param description description
     * @param state       state as string (e.g., "ToDo")
     */
    public record RowDTO(int id, String title, String description, String state) { }

    // =====================
    // Construction
    // =====================

    /**
     * Constructs the ViewModel with a DAO instance.
     *
     * @param dao tasks DAO (must not be null)
     * @throws NullPointerException if {@code dao} is null
     */
    public TasksViewModel(ITasksDAO dao) {
        this.dao = Objects.requireNonNull(dao, "ITasksDAO must not be null");
        updateCommandAvailability();
    }

    // =====================
    // Observer accessors (UI binds to these)
    // =====================

    /** Property that emits unfiltered rows whenever data changes. */
    public Property<List<RowDTO>> rowsProperty() { return rowsProperty; }

    /** Property that emits filtered rows whenever data or filter changes. */
    public Property<List<RowDTO>> filteredRowsProperty() { return filteredRowsProperty; }

    /** Emits true/false when undo availability changes. */
    public Property<Boolean> canUndoProperty() { return canUndoProperty; }

    /** Emits true/false when redo availability changes. */
    public Property<Boolean> canRedoProperty() { return canRedoProperty; }

    // =====================
    // Sorting API
    // =====================

    /** Sets the active sorting strategy (null clears). Fires properties. */
    public void setSortStrategy(SortStrategy strategy) {
        this.currentSort = Optional.ofNullable(strategy);
        rowsProperty.setValue(buildAllRowsSorted());
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
    }

    /** Clears sorting and reverts to DAO order. Fires properties. */
    public void clearSortStrategy() { setSortStrategy(null); }

    // =====================
    // Query side (read)
    // =====================

    /**
     * Reloads tasks from the DAO into an internal snapshot and updates observable properties.
     *
     * @throws TasksDAOException if the DAO read fails
     */
    public void reload() throws TasksDAOException {
        ITask[] arr = dao.getTasks();
        this.snapshot = (arr == null) ? List.of() : Arrays.asList(arr);

        rowsProperty.setValue(buildAllRowsSorted());
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
        updateCommandAvailability();
    }

    /**
     * Returns a UI-friendly immutable snapshot.
     * <p>Call {@link #reload()} beforehand if you need fresh data.</p>
     *
     * @return immutable list of {@link RowDTO}
     */
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

    /** Returns a single row by id for edit dialogs (no domain leakage). */
    public Optional<RowDTO> findRowById(int id) {
        validateId(id);
        for (ITask t : snapshot) {
            if (t.getId() == id) return Optional.of(toRowDTO(t));
        }
        return Optional.empty();
    }

    /** Returns available states for UI combo-box binding. */
    public TaskState[] getAvailableStates() { return TaskState.values(); }

    // =====================
    // Combinator Filters API (UI)
    // =====================

    /**
     * Sets the active Combinator filter (already composed with AND/OR on the caller side)
     * and updates the filtered property immediately.
     *
     * @param filter non-null ITaskFilter
     * @throws NullPointerException if filter is null
     */
    public void setFilter(ITaskFilter filter) {
        this.activeFilter = Optional.of(Objects.requireNonNull(filter, "filter must not be null"));
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
    }

    /** Clears any active filter and updates the filtered property to mirror unfiltered rows. */
    public void clearFilter() {
        this.activeFilter = Optional.empty();
        filteredRowsProperty.setValue(buildFilteredRowsSorted());
    }

    /**
     * Returns a UI-friendly snapshot filtered with the active Combinator filter (if any).
     * <p>Call {@link #reload()} beforehand if you need fresh data.</p>
     *
     * @return immutable list of {@link RowDTO} after filter; or all rows if no filter set.
     */
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

    // =====================
    // Command side (write) — all via CommandStack for Undo/Redo
    // =====================

    /**
     * Adds a new task via {@link AddTaskCommand}. DAO assigns the real id.
     *
     * @param title       non-empty title
     * @param description nullable description
     * @param state       non-null state
     * @throws CommandException if validation/DAO fails
     */
    public void addTask(String title, String description, TaskState state) throws CommandException {
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        Task toAdd = new Task(0, title, description, state); // id=0 => not yet persisted
        commands.execute(new AddTaskCommand(dao, toAdd));
        refreshAfterMutation();
    }

    /**
     * Updates an existing task via {@link UpdateTaskCommand}.
     *
     * @param id          existing task id
     * @param title       non-empty title
     * @param description nullable description
     * @param state       non-null state
     * @throws CommandException if task not found or validation/DAO fails
     */
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
        if (before == null) {
            throw new CommandException("Task not found for update: id=" + id);
        }

        Task after = new Task(id, title, description, state);
        commands.execute(new UpdateTaskCommand(dao, before, after));
        refreshAfterMutation();
    }

    /**
     * Deletes a task via {@link DeleteTaskCommand}.
     *
     * @param id existing task id
     * @throws CommandException if task not found or DAO fails
     */
    public void deleteTask(int id) throws CommandException {
        validateId(id);
        final ITask snapshotToDelete;
        try {
            snapshotToDelete = dao.getTask(id);
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to load task for delete: id=" + id, e);
        }
        if (snapshotToDelete == null) {
            throw new CommandException("Task not found for delete: id=" + id);
        }
        commands.execute(new DeleteTaskCommand(dao, snapshotToDelete));
        refreshAfterMutation();
    }


    /**
     * Deletes multiple tasks via individual DeleteTaskCommand executions,
     * skipping IDs that do not exist in the DAO, and refreshing once.
     */
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
                // Some DAO implementations (e.g., Derby) throw on "not found".
                if (isNotFound(e)) {
                    continue; // skip silently
                }
                throw new CommandException("Failed to load task for delete: id=" + id, e);
            }
            if (t != null) {
                commands.execute(new DeleteTaskCommand(dao, t));
                changed = true;
            }
        }
        if (changed) refreshAfterMutation();
    }

    /** Varargs convenience for UI selections. */
    public void deleteTasks(int... ids) throws CommandException {
        if (ids == null || ids.length == 0) return;
        List<Integer> list = new ArrayList<>(ids.length);
        for (int id : ids) list.add(id);
        deleteTasks(list);
    }

    /**
     * Deletes all tasks via DAO and refreshes observable state.
     *
     * @throws TasksDAOException if the DAO operation fails
     */
    public void deleteAll() throws TasksDAOException {
        dao.deleteTasks();
        reload(); // refresh lists + fire properties + update command availability
    }

    /**
     * Marks task state (explicit target) via {@link MarkStateCommand}.
     *
     * @param id    existing task id
     * @param state non-null target state
     * @throws CommandException if task not found, illegal transition, or DAO fails
     */
    public void markState(int id, TaskState state) throws CommandException {
        transitionState(id, state);
    }

    /**
     * State-pattern API: transition to a target state if allowed by current state's rules.
     * Executes via {@link MarkStateCommand} to support undo/redo.
     *
     * @param id     existing task id
     * @param target desired target state (non-null)
     * @throws CommandException if task not found or transition is not allowed or DAO fails
     */
    public void transitionState(int id, TaskState target) throws CommandException {
        validateId(id);
        Objects.requireNonNull(target, "target must not be null");

        final ITask before;
        try {
            before = dao.getTask(id);
        } catch (TasksDAOException e) {
            throw new CommandException("Failed to load task for state change: id=" + id, e);
        }
        if (before == null) {
            throw new CommandException("Task not found: id=" + id);
        }

        final TaskState current = Objects.requireNonNull(before.getState(), "current state must not be null");
        if (!current.canTransitionTo(target)) {
            throw new CommandException("Illegal state transition: " + current + " -> " + target);
        }

        // Factory that creates a new snapshot with the requested state (validation in Task constructor)
        MarkStateCommand.TaskFactory factory = (src, newState) ->
                new Task(src.getId(), safe(src.getTitle()), safe(src.getDescription()), newState);

        commands.execute(new MarkStateCommand(dao, before, target, factory));
        refreshAfterMutation();
    }

    /**
     * Advances task to the next legal state according to current state's behavior.
     * Executes via {@link MarkStateCommand} to support undo/redo.
     *
     * @param id existing task id
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
        if (before == null) {
            throw new CommandException("Task not found: id=" + id);
        }

        final TaskState current = Objects.requireNonNull(before.getState(), "current state must not be null");
        final TaskState next = current.next();

        MarkStateCommand.TaskFactory factory = (src, newState) ->
                new Task(src.getId(), safe(src.getTitle()), safe(src.getDescription()), newState);

        commands.execute(new MarkStateCommand(dao, before, next, factory));
        refreshAfterMutation();
    }

    // =====================
    // Undo / Redo API
    // =====================

    /** @return true if at least one command can be undone. */
    public boolean canUndo() { return commands.canUndo(); }

    /** @return true if at least one command can be redone. */
    public boolean canRedo() { return commands.canRedo(); }

    /**
     * Undoes the last executed command and refreshes observable properties.
     * @throws CommandException if undo fails
     */
    public void undo() throws CommandException {
        commands.undo();
        refreshAfterMutation();
    }

    /**
     * Redoes the last undone command and refreshes observable properties.
     * @throws CommandException if redo fails
     */
    public void redo() throws CommandException {
        commands.redo();
        refreshAfterMutation();
    }

    // =====================
    // Export & Reports (Visitor + Records)
    // =====================

    /**
     * Returns a ByStateCount report directly for UI consumption (no file IO).
     * If {@code useFiltered} is true, applies the current active filter over the in-memory snapshot.
     *
     * @param useFiltered whether to count only the filtered subset
     * @return immutable {@link ByStateCount} value object
     */
    public ByStateCount getCountsByState(boolean useFiltered) {
        final CountByStateVisitor v = new CountByStateVisitor();
        if (useFiltered && activeFilter.isPresent()) {
            final ITaskFilter f = activeFilter.get();
            for (ITask t : snapshot) if (f.test(t)) t.accept(v);
        } else {
            for (ITask t : snapshot) t.accept(v);
        }
        v.complete();
        return v.result();
    }

    /**
     * Convenience: returns counts as a map for UI widgets that bind to key/value.
     *
     * @param useFiltered whether to count only the filtered subset
     * @return EnumMap with counts per {@link TaskState}
     */
    public EnumMap<TaskState, Integer> getCountsMapByState(boolean useFiltered) {
        final ByStateCount r = getCountsByState(useFiltered);
        final EnumMap<TaskState, Integer> m = new EnumMap<>(TaskState.class);
        m.put(TaskState.ToDo, r.todo());
        m.put(TaskState.InProgress, r.inProgress());
        m.put(TaskState.Completed, r.completed());
        return m;
    }

    /**
     * Exports tasks (CSV/TXT) via Visitor (records & pattern matching).
     * Prefer the overload with {@code filteredIds} to export the table subset.
     *
     * @param path        output file path
     * @param format      CSV or TXT (see {@link ExportFormat})
     * @param useFiltered true to export a subset (see overload)
     * @throws IOException       if writing fails
     * @throws TasksDAOException if DAO access fails
     */
    public void exportTasks(Path path, ExportFormat format, boolean useFiltered)
            throws IOException, TasksDAOException {
        exportTasks(path, format, useFiltered, null);
    }

    /**
     * Exports tasks (CSV/TXT) via Visitor (records & pattern matching).
     * When {@code useFiltered} is true and {@code filteredIds} provided,
     * only those IDs are exported. Otherwise, all tasks are exported.
     *
     * @param path        output file path
     * @param format      CSV or TXT
     * @param useFiltered whether to export a filtered subset
     * @param filteredIds optional list of task IDs
     * @throws IOException       if writing fails
     * @throws TasksDAOException if DAO access fails
     */
    public void exportTasks(Path path, ExportFormat format, boolean useFiltered, List<Integer> filteredIds)
            throws IOException, TasksDAOException {

        final List<ITask> tasks = loadTasksForExport(useFiltered, filteredIds);

        final String content;
        if (format == ExportFormat.CSV) {
            final var v = new CsvFlatTaskVisitorImpl();
            for (ITask t : tasks) t.accept(v);
            v.complete();
            content = v.result();
        } else {
            final var v = new PlainTextFlatTaskVisitorImpl();
            for (ITask t : tasks) t.accept(v);
            v.complete();
            content = v.result();
        }

        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Exports a “count by state” report via Visitor + exporters.
     *
     * @param path        output file path
     * @param format      CSV or TXT
     * @param useFiltered whether to export a filtered subset
     * @param filteredIds optional list of task IDs
     * @throws IOException       if writing fails
     * @throws TasksDAOException if DAO access fails
     */
    public void exportByStateReport(Path path, ExportFormat format, boolean useFiltered, List<Integer> filteredIds)
            throws IOException, TasksDAOException {
        final List<ITask> tasks = loadTasksForExport(useFiltered, filteredIds);

        final CountByStateVisitor visitor = new CountByStateVisitor();
        for (ITask t : tasks) t.accept(visitor);
        visitor.complete();
        final ByStateCount report = visitor.result();

        final IReportExporter<ByStateCount> exporter =
                (format == ExportFormat.CSV) ? new ByStateCsvExporter() : new ByStatePlainTextExporter();
        final String content = exporter.export(report);

        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    // =====================
    // Helpers & validation
    // =====================

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
        // Fallback / export-all
        final ITask[] arr = dao.getTasks();
        return (arr == null) ? List.of() : Arrays.asList(arr);
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

    /** Maps a domain ITask to RowDTO (null-safe fields). */
    private static RowDTO toRowDTO(ITask t) {
        return new RowDTO(
                t.getId(),
                safe(t.getTitle()),
                safe(t.getDescription()),
                (t.getState() == null) ? "" : t.getState().name()
        );
    }

    private List<RowDTO> buildAllRows() {
        final List<RowDTO> out = new ArrayList<>(snapshot.size());
        for (ITask t : snapshot) out.add(toRowDTO(t));
        return Collections.unmodifiableList(out);
    }

    private List<RowDTO> buildFilteredRows(List<RowDTO> allUnfiltered) {
        if (activeFilter.isEmpty()) return allUnfiltered;
        final ITaskFilter f = activeFilter.get();
        final List<RowDTO> out = new ArrayList<>();
        for (ITask t : snapshot) if (f.test(t)) out.add(toRowDTO(t));
        return Collections.unmodifiableList(out);
    }

    /** Returns a (possibly) sorted snapshot of tasks according to current strategy. */
    private List<ITask> sortedSnapshot() {
        final List<ITask> base = (snapshot == null) ? List.of() : snapshot;
        return currentSort.map(s -> s.sort(base)).orElse(base);
    }

    /** Builds unfiltered rows with optional sorting applied. */
    private List<RowDTO> buildAllRowsSorted() {
        final List<ITask> tasks = sortedSnapshot();
        final List<RowDTO> out = new ArrayList<>(tasks.size());
        for (ITask t : tasks) out.add(toRowDTO(t));
        return Collections.unmodifiableList(out);
    }

    /** Builds filtered rows with optional sorting applied (filter first, then sort). */
    private List<RowDTO> buildFilteredRowsSorted() {
        // 1) filter tasks from the raw snapshot (not pre-sorted, to avoid bias)
        final List<ITask> filtered = new ArrayList<>();
        final var opt = this.activeFilter;
        if (opt.isEmpty()) {
            filtered.addAll((snapshot == null) ? List.of() : snapshot);
        } else {
            final ITaskFilter f = opt.get();
            for (ITask t : snapshot) if (f.test(t)) filtered.add(t);
        }

        // 2) sort filtered tasks if strategy present
        final List<ITask> ordered = currentSort.map(s -> s.sort(filtered)).orElse(filtered);

        // 3) map to rows
        final List<RowDTO> out = new ArrayList<>(ordered.size());
        for (ITask t : ordered) out.add(toRowDTO(t));
        return Collections.unmodifiableList(out);
    }

    /** Heuristic check for "not found" DAO error to allow graceful skipping. */
    private static boolean isNotFound(TasksDAOException e) {
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase(Locale.ROOT).contains("not found");
    }

    // =====================
    // Private Visitors (flat CSV/TXT)
    // =====================

    /**
     * Produces CSV rows by visiting export record variants.
     * Kept private to avoid leaking domain/UI coupling.
     */
    private static final class CsvFlatTaskVisitorImpl implements TaskVisitor {
        private final StringBuilder sb = new StringBuilder("id,title,description,state\n");

        private static String esc(String s) {
            if (s == null) return "";
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }

        private void addRow(int id, String title, String desc, String state) {
            sb.append(id).append(',')
                    .append(esc(title)).append(',')
                    .append(esc(desc)).append(',')
                    .append(esc(state)).append('\n');
        }

        @Override public void visit(ToDoTaskRec node)       { addRow(node.id(), node.title(), node.description(), node.state().name()); }
        @Override public void visit(InProgressTaskRec node) { addRow(node.id(), node.title(), node.description(), node.state().name()); }
        @Override public void visit(CompletedTaskRec node)  { addRow(node.id(), node.title(), node.description(), node.state().name()); }
        @Override public void complete() { /* no-op */ }
        public String result() { return sb.toString(); }
    }

    /** Produces plain-text blocks by visiting export record variants. */
    private static final class PlainTextFlatTaskVisitorImpl implements TaskVisitor {
        private final StringBuilder sb = new StringBuilder("Tasks Export\n------------\n");

        private static String nz(String s) { return (s == null) ? "" : s; }

        private void addBlock(int id, String title, String desc, String state) {
            sb.append("ID: ").append(id).append('\n')
                    .append("Title: ").append(nz(title)).append('\n')
                    .append("Description: ").append(nz(desc)).append('\n')
                    .append("State: ").append(nz(state)).append("\n\n");
        }

        @Override public void visit(ToDoTaskRec node)       { addBlock(node.id(), node.title(), node.description(), node.state().name()); }
        @Override public void visit(InProgressTaskRec node) { addBlock(node.id(), node.title(), node.description(), node.state().name()); }
        @Override public void visit(CompletedTaskRec node)  { addBlock(node.id(), node.title(), node.description(), node.state().name()); }
        @Override public void complete() { /* no-op */ }
        public String result() { return sb.toString(); }
    }
}
