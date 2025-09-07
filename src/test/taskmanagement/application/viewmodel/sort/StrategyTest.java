package taskmanagement.application.viewmodel.sort;

import org.junit.Test;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.visitor.TaskVisitor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * StrategyTest
 * ------------
 * Unit tests for sorting strategies:
 *  • SortById    – ascending by id
 *  • SortByTitle – case-insensitive lexicographic by title (preferred policy)
 *  • SortByState – lifecycle order: ToDo < InProgress < Completed
 *
 * The test is resilient to two common API shapes:
 *  1) Strategy implements Comparator<ITask>  → Collections.sort(list, strategy)
 *  2) Strategy exposes either:
 *       - List<ITask> sort(List<ITask>)      → use directly
 *       - Comparator<ITask> comparator()     → use Collections.sort with returned comparator
 */
public final class StrategyTest {

    /** Minimal ITask test-double used only for sorting verification. */
    private static final class T implements ITask {
        private final int id;
        private final String title;
        private final String description;
        private final TaskState state;

        T(int id, String title, String description, TaskState state) {
            this.id = id;
            this.title = Objects.requireNonNull(title);
            this.description = Objects.requireNonNull(description);
            this.state = Objects.requireNonNull(state);
        }

        @Override public int getId() { return id; }
        @Override public String getTitle() { return title; }
        @Override public String getDescription() { return description; }
        @Override public TaskState getState() { return state; }
        @Override public void accept(TaskVisitor visitor) { /* not used */ }

        @Override public String toString() { return "T{id=" + id + ", title='" + title + "', state=" + state + '}'; }
    }

    // ---------- helpers ----------

    /** Applies a SortStrategy to a copy of src regardless of its exact API. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<ITask> apply(SortStrategy strategy, List<ITask> src) {
        // 1) Strategy is a Comparator
        if (strategy instanceof Comparator) {
            List<ITask> copy = new ArrayList<>(src);
            copy.sort((Comparator) strategy);
            return copy;
        }

        // 2) sort(List<ITask>)
        try {
            Method m = strategy.getClass().getMethod("sort", List.class);
            Object out = m.invoke(strategy, new ArrayList<>(src));
            if (out instanceof List) return (List<ITask>) out;
        } catch (NoSuchMethodException ignore) {
            // fall through
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed invoking sort(List): " + e.getMessage(), e);
        }

        // 3) comparator()
        try {
            Method m = strategy.getClass().getMethod("comparator");
            Object cmp = m.invoke(strategy);
            if (cmp instanceof Comparator) {
                List<ITask> copy = new ArrayList<>(src);
                copy.sort((Comparator<? super ITask>) cmp);
                return copy;
            }
        } catch (NoSuchMethodException ignore) {
            // fall through
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed invoking comparator(): " + e.getMessage(), e);
        }

        throw new AssertionError("Unknown SortStrategy API: " + strategy.getClass().getName());
    }

    private static List<Integer> ids(List<ITask> list)   { return list.stream().map(ITask::getId).collect(Collectors.toList()); }
    private static List<String>  titles(List<ITask> list) { return list.stream().map(ITask::getTitle).collect(Collectors.toList()); }
    private static List<TaskState> states(List<ITask> list){ return list.stream().map(ITask::getState).collect(Collectors.toList()); }

    private static List<ITask> sample() {
        return List.of(
                new T(3, "Bravo",  "b", TaskState.InProgress),
                new T(1, "alpha",  "a", TaskState.ToDo),
                new T(2, "Charlie","c", TaskState.Completed),
                new T(5, "alpha",  "x", TaskState.Completed),
                new T(4, "delta",  "d", TaskState.ToDo)
        );
    }

    // ---------- tests: SortById ----------

    @Test
    public void sortById_ascending() {
        SortStrategy s = new SortById();
        List<ITask> sorted = apply(s, sample());
        assertEquals(List.of(1, 2, 3, 4, 5), ids(sorted));
    }

    @Test
    public void sortById_emptyAndSingle_areStable() {
        SortStrategy s = new SortById();
        assertTrue(apply(s, List.of()).isEmpty());
        ITask one = new T(7, "x", "d", TaskState.ToDo);
        assertEquals(List.of(one), apply(s, List.of(one)));
    }

    // ---------- tests: SortByTitle (preferred: CASE-INSENSITIVE) ----------

    @Test
    public void sortByTitle_lexicographic_caseInsensitive() {
        SortStrategy s = new SortByTitle();
        List<ITask> sorted = apply(s, sample());
        List<String> actual = titles(sorted);

        // Build expected dynamically by case-insensitive sort
        List<String> expected = new ArrayList<>(titles(sample()));
        expected.sort(String.CASE_INSENSITIVE_ORDER);

        assertEquals("titles should be sorted case-insensitively", expected, actual);

        // sanity: both "alpha" adjacent
        int i = actual.indexOf("alpha"), j = actual.lastIndexOf("alpha");
        assertTrue("alphas should be adjacent", j - i == 1);
    }

    @Test
    public void sortByTitle_emptyAndSingle_areStable() {
        SortStrategy s = new SortByTitle();
        assertTrue(apply(s, List.of()).isEmpty());
        ITask one = new T(9, "Only", "d", TaskState.Completed);
        assertEquals(List.of("Only"), titles(apply(s, List.of(one))));
    }

    // ---------- tests: SortByState ----------

    @Test
    public void sortByState_order_ToDo_InProgress_Completed() {
        SortStrategy s = new SortByState();
        List<ITask> sorted = apply(s, sample());
        List<TaskState> st = states(sorted);

        int firstIP = st.indexOf(TaskState.InProgress);
        int firstC  = st.indexOf(TaskState.Completed);

        for (int k = 0; k < firstIP; k++)            assertEquals(TaskState.ToDo, st.get(k));
        for (int k = firstIP; k < firstC; k++)       assertEquals(TaskState.InProgress, st.get(k));
        for (int k = firstC; k < st.size(); k++)     assertEquals(TaskState.Completed, st.get(k));
    }

    @Test
    public void sortByState_emptyAndSingle_areStable() {
        SortStrategy s = new SortByState();
        assertTrue(apply(s, List.of()).isEmpty());
        ITask one = new T(11, "one", "d", TaskState.InProgress);
        assertEquals(List.of(TaskState.InProgress), states(apply(s, List.of(one))));
    }
}
