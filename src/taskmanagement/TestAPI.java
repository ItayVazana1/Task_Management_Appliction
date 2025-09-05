package taskmanagement;

import taskmanagement.application.viewmodel.ExportFormat;
import taskmanagement.application.viewmodel.TasksViewModel;
import taskmanagement.application.viewmodel.events.Property;
import taskmanagement.application.viewmodel.sort.SortStrategy;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.filter.ITaskFilter;
import taskmanagement.domain.visitor.reports.ByStateCount;
import taskmanagement.persistence.DAOProvider;
import taskmanagement.persistence.ITasksDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class TestAPI {

    // Base folder: Desktop/TaskExports, plus subfolder per run timestamp.
    private static final Path BASE_DIR =
            Paths.get(System.getProperty("user.home"), "Desktop", "TaskExports");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Simple logger that writes to both console and a buffer we flush to file at the end.
    private static final StringBuilder LOG = new StringBuilder();
    private static Path runDir; // e.g. Desktop/TaskExports/20250906_141122
    private static String runTs;

    public static void main(String[] args) throws Exception {
        // Prepare run directory
        Files.createDirectories(BASE_DIR);
        runTs = LocalDateTime.now().format(TS);
        runDir = BASE_DIR.resolve(runTs);
        Files.createDirectories(runDir);

        log("== TestAPI run started ==");
        log("Run folder: " + runDir.toAbsolutePath());

        // Wire DAO + VM
        ITasksDAO dao = DAOProvider.get(); // real Derby via provider
        TasksViewModel vm = new TasksViewModel(dao);
        subscribeForDebug(vm);

        int stepNo = 1;

        // 0) Clean slate
        step(stepNo++, "deleteAll (clean slate)");
        vm.deleteAll();
        vm.reload();
        assertEq(vm.getRows().size(), 0, "rows after deleteAll");
        exportAll(vm, name(stepNo, "initial_empty"));

        // 1) Seed: add baseline tasks
        step(stepNo++, "addTask x3");
        vm.addTask("Alpha", "first task", TaskState.ToDo);
        vm.addTask("Beta", "second task", TaskState.InProgress);
        vm.addTask("Gamma", "third task", TaskState.Completed);
        exportAll(vm, name(stepNo, "after_add3"));

        // 2) Update Beta -> Beta+
        step(stepNo++, "updateTask (rename Beta -> Beta+)");
        int betaId = findByTitle(vm, "Beta").id();
        vm.updateTask(betaId, "Beta+", "updated desc", TaskState.InProgress);
        assertTrue(findByTitle(vm, "Beta+").description().contains("updated"), "updated description visible");
        exportAll(vm, name(stepNo, "after_update_beta"));

        // 3) Transition Alpha: ToDo -> InProgress
        step(stepNo++, "transitionState (Alpha: ToDo -> InProgress)");
        int alphaId = findByTitle(vm, "Alpha").id();
        vm.transitionState(alphaId, TaskState.InProgress);
        exportAll(vm, name(stepNo, "after_transition_alpha"));

        // 4) Advance Alpha: InProgress -> Completed
        step(stepNo++, "advanceState (Alpha: InProgress -> Completed)");
        vm.advanceState(alphaId);
        exportAll(vm, name(stepNo, "after_advance_alpha"));

        // 5) Delete single (Gamma)
        step(stepNo++, "deleteTask (Gamma)");
        int gammaId = findByTitle(vm, "Gamma").id();
        vm.deleteTask(gammaId);
        exportAll(vm, name(stepNo, "after_delete_gamma"));

        // 6) Delete multiple (only existing ids)
        step(stepNo++, "deleteTasks (multi; only existing ids)");
        vm.addTask("Delta", "aux", TaskState.ToDo);
        int deltaId = findByTitle(vm, "Delta").id();
        vm.deleteTasks(Collections.singletonList(deltaId));
        exportAll(vm, name(stepNo, "after_delete_multi"));

        // 7) Undo
        step(stepNo++, "undo last delete");
        vm.undo();
        exportAll(vm, name(stepNo, "after_undo"));

        // 8) Redo
        step(stepNo++, "redo last undo");
        vm.redo();
        exportAll(vm, name(stepNo, "after_redo"));

        // 9) Log counts
        step(stepNo++, "counts by state (log only)");
        ByStateCount countsAll = vm.getCountsByState(false);
        log("Counts (all): ToDo=" + countsAll.todo() + ", InProgress=" + countsAll.inProgress() + ", Completed=" + countsAll.completed());

        // 10) Filter (contains 'a') and export FILTERED snapshot
        step(stepNo++, "setFilter (title contains 'a' case-insensitive) + export filtered");
        ITaskFilter containsA = task -> {
            String t = task.getTitle();
            return t != null && t.toLowerCase(Locale.ROOT).contains("a");
        };
        vm.setFilter(containsA);
        exportFiltered(vm, name(stepNo, "filtered_contains_a"));

        // 11) Sort strategy on filtered + export
        step(stepNo++, "setSortStrategy (title ASC) + export filtered+sorted");
        vm.setSortStrategy(new SortStrategy() {
            @Override public List<ITask> sort(List<ITask> input) {
                List<ITask> copy = new ArrayList<>(input);
                copy.sort(Comparator.comparing(t -> nz(t.getTitle())));
                return copy;
            }
            @Override public String displayName() { return "Title ASC"; }
        });
        exportFiltered(vm, name(stepNo, "filtered_sorted_title_asc"));

        // 12) Clear filter/sort + export full
        step(stepNo++, "clear filter & sort + export full");
        vm.clearSortStrategy();
        vm.clearFilter();
        exportAll(vm, name(stepNo, "after_clear_filter_sort"));

        // 13) Final: deleteAll + export empty
        step(stepNo++, "deleteAll (final)");
        vm.deleteAll();
        vm.reload();
        exportAll(vm, name(stepNo, "final_empty"));

        // Write consolidated run log
        writeRunLog();

        log("== TestAPI run completed ==");
        log("Artifacts folder: " + runDir.toAbsolutePath());
    }

    // ---------- export helpers ----------

    /** Build file name like "03_after_update_20250906_141122.csv". */
    private static String name(int idx, String label) {
        return String.format("%02d_%s_%s.csv", idx, label, runTs);
    }

    /** Export entire table (unfiltered). */
    private static void exportAll(TasksViewModel vm, String fileName) throws Exception {
        Path out = runDir.resolve(fileName);
        vm.exportTasks(out, ExportFormat.CSV, false);
        long sz = Files.size(out);
        assertTrue(sz > 0, "CSV(all) written: " + out.toAbsolutePath());
        log("CSV(all): " + out.toAbsolutePath() + " (" + sz + " bytes)");
    }

    /** Export current filtered view by passing filtered IDs. */
    private static void exportFiltered(TasksViewModel vm, String fileName) throws Exception {
        Path out = runDir.resolve(fileName);
        List<Integer> ids = vm.getFilteredRows().stream().map(TasksViewModel.RowDTO::id).toList();
        vm.exportTasks(out, ExportFormat.CSV, true, ids);
        long sz = Files.size(out);
        assertTrue(sz > 0, "CSV(filtered) written: " + out.toAbsolutePath());
        log("CSV(filtered): " + out.toAbsolutePath() + " (" + sz + " bytes)");
    }

    /** Persist the accumulated log to run_dir/run_log.txt */
    private static void writeRunLog() {
        Path logFile = runDir.resolve("run_log.txt");
        try {
            Files.writeString(logFile, LOG.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            // Also echo final location
            System.out.println("Run log written: " + logFile.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    // ---------- debug subscriptions using Property<T> (old,new) ----------

    private static void subscribeForDebug(TasksViewModel vm) {
        Property<List<TasksViewModel.RowDTO>> rows = vm.rowsProperty();
        rows.addListener((oldV, newV) -> log("[rows] size=" + (newV == null ? 0 : newV.size())));
        Property<List<TasksViewModel.RowDTO>> filtered = vm.filteredRowsProperty();
        filtered.addListener((oldV, newV) -> log("[filtered] size=" + (newV == null ? 0 : newV.size())));
        Property<Boolean> canUndo = vm.canUndoProperty();
        canUndo.addListener((oldV, newV) -> log("[canUndo] " + (newV == null ? false : newV)));
        Property<Boolean> canRedo = vm.canRedoProperty();
        canRedo.addListener((oldV, newV) -> log("[canRedo] " + (newV == null ? false : newV)));
    }

    // ---------- helpers ----------

    private static String nz(String s) { return s == null ? "" : s; }

    private static TasksViewModel.RowDTO findByTitle(TasksViewModel vm, String title) {
        return vm.getRows().stream()
                .filter(r -> r.title().equals(title))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Row with title '" + title + "' not found"));
    }

    private static void step(int idx, String name) {
        log("\n-- " + String.format("%02d", idx) + " :: " + name + " --");
    }

    private static void log(String s) {
        System.out.println(s);
        LOG.append(s).append(System.lineSeparator());
    }

    private static void assertEq(int actual, int expected, String label) {
        if (actual != expected)
            throw new AssertionError(label + " — expected " + expected + " but was " + actual);
        log("OK: " + label + " = " + actual);
    }

    private static void assertTrue(boolean cond, String label) {
        if (!cond) throw new AssertionError("Expected TRUE: " + label);
        log("OK: " + label);
    }
}
