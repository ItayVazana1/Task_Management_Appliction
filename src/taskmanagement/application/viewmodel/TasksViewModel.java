package taskmanagement.application.viewmodel;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.TaskVisitor;
import taskmanagement.domain.visitor.CountByStateVisitor;
import taskmanagement.domain.visitor.adapters.ByStateCsvExporter;
import taskmanagement.domain.visitor.adapters.ByStatePlainTextExporter;
import taskmanagement.domain.visitor.adapters.IReportExporter;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.domain.visitor.export.CompletedTaskRec;
import taskmanagement.domain.visitor.export.InProgressTaskRec;
import taskmanagement.domain.visitor.export.ToDoTaskRec;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * MVVM ViewModel for the Tasks screen.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Mediate between the UI and the DAO-backed model.</li>
 *   <li>Expose read-only task snapshots suitable for the UI (no domain leakage).</li>
 *   <li>Provide imperative commands for CRUD and state changes.</li>
 *   <li>Provide export APIs implemented via Visitor + Records & Pattern Matching.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * - The UI never touches {@link ITask} or {@link Task} directly.
 * - After each mutation, {@link #reload()} is called so the UI sees generated IDs and the latest state.
 */
public final class TasksViewModel {

    /** DAO dependency (model). */
    private final ITasksDAO dao;

    /** Cached snapshot of tasks for UI consumption (latest reload). */
    private List<ITask> snapshot = Collections.emptyList();

    /**
     * Immutable UI row. Keeps the UI decoupled from domain types.
     *
     * @param id          task id
     * @param title       title
     * @param description description
     * @param state       state as string (e.g., "ToDo")
     */
    public record RowDTO(int id, String title, String description, String state) { }

    /**
     * Constructs the ViewModel with a DAO instance.
     *
     * @param dao tasks DAO (must not be null)
     */
    public TasksViewModel(ITasksDAO dao) {
        this.dao = Objects.requireNonNull(dao, "ITasksDAO must not be null");
    }

    // =====================
    // Query side (read)
    // =====================

    /**
     * Reloads tasks from the DAO into an internal snapshot.
     *
     * @throws TasksDAOException if the DAO read fails
     */
    public void reload() throws TasksDAOException {
        ITask[] arr = dao.getTasks();
        this.snapshot = (arr == null) ? List.of() : Arrays.asList(arr);
    }

    /**
     * Returns a UI-friendly snapshot of tasks.
     * <p>
     * Call {@link #reload()} before this if you want fresh data.
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

    // =====================
    // Command side (write)
    // =====================

    /**
     * Adds a new task.
     *
     * @param title       non-empty title
     * @param description nullable/optional description
     * @param state       non-null state
     * @throws Exception wraps {@link TasksDAOException} or validation exceptions from the domain model
     */
    public void addTask(String title, String description, TaskState state) throws Exception {
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        // id=0 indicates "not yet persisted"; DAO should assign a real id.
        Task toAdd = new Task(0, title, description, state);
        dao.addTask(toAdd);
        reload();
    }

    /**
     * Updates an existing task by id.
     *
     * @param id          existing task id
     * @param title       non-empty title
     * @param description nullable/optional description
     * @param state       non-null state
     * @throws Exception wraps {@link TasksDAOException} or validation exceptions from the domain model
     */
    public void updateTask(int id, String title, String description, TaskState state) throws Exception {
        validateId(id);
        validateTitle(title);
        Objects.requireNonNull(state, "state must not be null");

        Task updated = new Task(id, title, description, state);
        dao.updateTask(updated);
        reload();
    }

    /**
     * Deletes a task by id.
     *
     * @param id existing task id
     * @throws TasksDAOException if the DAO operation fails
     */
    public void deleteTask(int id) throws TasksDAOException {
        validateId(id);
        dao.deleteTask(id);
        reload();
    }

    /**
     * Marks task state by id.
     *
     * @param id    existing task id
     * @param state non-null state
     * @throws Exception wraps {@link TasksDAOException} or validation exceptions
     */
    public void markState(int id, TaskState state) throws Exception {
        validateId(id);
        Objects.requireNonNull(state, "state must not be null");

        // Fetch existing task, update only state, persist.
        ITask original = dao.getTask(id);
        if (original == null) {
            throw new TasksDAOException("Task not found: id=" + id);
        }
        Task updated = new Task(
                original.getId(),
                safe(original.getTitle()),
                safe(original.getDescription()),
                state
        );
        dao.updateTask(updated);
        reload();
    }

    // =====================
    // Export (Visitor + Records)
    // =====================

    /**
     * Exports tasks to CSV/TXT via Visitor (records & pattern matching).
     * <p>
     * If {@code useFiltered} is {@code true} and you want to export exactly what's shown
     * in the UI table, prefer the overload that receives {@code filteredIds}.
     *
     * @param path output file path
     * @param format CSV or TXT (see {@link ExportFormat})
     * @param useFiltered true to export a subset; see overload with {@code filteredIds}
     * @throws IOException if writing fails
     * @throws TasksDAOException if DAO access fails
     */
    public void exportTasks(Path path, ExportFormat format, boolean useFiltered)
            throws IOException, TasksDAOException {
        exportTasks(path, format, useFiltered, null);
    }

    /**
     * Exports tasks to CSV/TXT via Visitor (records & pattern matching).
     * <p>
     * When {@code useFiltered} is true and {@code filteredIds} is provided,
     * only those task IDs are exported. Otherwise, all tasks are exported.
     *
     * @param path output file path
     * @param format CSV or TXT
     * @param useFiltered whether to export a filtered subset
     * @param filteredIds optional list of task IDs to export when {@code useFiltered} is true
     * @throws IOException if writing fails
     * @throws TasksDAOException if DAO access fails
     */
    public void exportTasks(Path path, ExportFormat format, boolean useFiltered, List<Integer> filteredIds)
            throws IOException, TasksDAOException {

        final List<ITask> tasks = loadTasksForExport(useFiltered, filteredIds);

        final String content;
        if (format == ExportFormat.CSV) {
            final var v = new CsvFlatTaskVisitorImpl();
            for (ITask t : tasks) {
                t.accept(v); // Task.accept creates a record and dispatches
            }
            v.complete();
            content = v.result();
        } else {
            final var v = new PlainTextFlatTaskVisitorImpl();
            for (ITask t : tasks) {
                t.accept(v);
            }
            v.complete();
            content = v.result();
        }

        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Exports a "count by state" report via Visitor + adapters you already have.
     *
     * @param path output file path
     * @param format CSV or TXT
     * @param useFiltered whether to export a filtered subset
     * @param filteredIds optional list of task IDs to export when {@code useFiltered} is true
     * @throws IOException if writing fails
     * @throws TasksDAOException if DAO access fails
     */
    public void exportByStateReport(Path path, ExportFormat format, boolean useFiltered, List<Integer> filteredIds)
            throws IOException, TasksDAOException {
        final List<ITask> tasks = loadTasksForExport(useFiltered, filteredIds);

        final CountByStateVisitor visitor = new CountByStateVisitor();
        for (ITask t : tasks) {
            t.accept(visitor); // record-based visit increments counters
        }
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

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be empty");
        }
    }

    private static void validateId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("invalid id: " + id);
        }
    }

    // =====================
    // Private Visitors (flat CSV/TXT)
    // =====================

    /**
     * Produces CSV content by visiting export record variants.
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
                    .append(esc(state))
                    .append('\n');
        }

        @Override public void visit(ToDoTaskRec node) {
            addRow(node.id(), node.title(), node.description(), node.state().name());
        }

        @Override public void visit(InProgressTaskRec node) {
            addRow(node.id(), node.title(), node.description(), node.state().name());
        }

        @Override public void visit(CompletedTaskRec node) {
            addRow(node.id(), node.title(), node.description(), node.state().name());
        }

        @Override public void complete() { /* no-op */ }

        public String result() { return sb.toString(); }
    }

    /**
     * Produces plain-text content by visiting export record variants.
     */
    private static final class PlainTextFlatTaskVisitorImpl implements TaskVisitor {
        private final StringBuilder sb = new StringBuilder("Tasks Export\n------------\n");

        private static String nz(String s) { return (s == null) ? "" : s; }

        private void addBlock(int id, String title, String desc, String state) {
            sb.append("ID: ").append(id).append('\n')
                    .append("Title: ").append(nz(title)).append('\n')
                    .append("Description: ").append(nz(desc)).append('\n')
                    .append("State: ").append(nz(state)).append("\n\n");
        }

        @Override public void visit(ToDoTaskRec node) {
            addBlock(node.id(), node.title(), node.description(), node.state().name());
        }

        @Override public void visit(InProgressTaskRec node) {
            addBlock(node.id(), node.title(), node.description(), node.state().name());
        }

        @Override public void visit(CompletedTaskRec node) {
            addBlock(node.id(), node.title(), node.description(), node.state().name());
        }

        @Override public void complete() { /* no-op */ }

        public String result() { return sb.toString(); }
    }
}
