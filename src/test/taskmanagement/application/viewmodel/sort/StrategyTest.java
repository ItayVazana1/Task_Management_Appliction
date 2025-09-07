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
 * JUnit 4 tests for sorting strategies: {@code SortById} (ascending by id),
 * {@code SortByTitle} (case-insensitive lexicographic order), and
 * {@code SortByState} (ToDo &lt; InProgress &lt; Completed).
 * <p>
 * The suite is compatible with multiple strategy API shapes:
 * a strategy may implement {@link Comparator}, expose {@code sort(List&lt;ITask&gt;)},
 * or expose {@code comparator()}.
 */
public final class StrategyTest {

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
        @Override public void accept(TaskVisitor visitor) { }

        @Override public String toString() { return "T{id=" + id + ", title='" + title + "', state=" + state + '}'; }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<ITask> apply(SortStrategy strategy, List<ITask> src) {
        if (strategy instanceof Comparator) {
            List<ITask> copy = new ArrayList<>(src);
            copy.sort((Comparator) strategy);
            return copy;
        }
        try {
            Method m = strategy.getClass().getMethod("sort", List.class);
            Object out = m.invoke(strategy, new ArrayList<>(src));
            if (out instanceof List) return (List<ITask>) out;
        } catch (NoSuchMethodException ignore) {
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed invoking sort(List): " + e.getMessage(), e);
        }
        try {
            Method m = strategy.getClass().getMethod("comparator");
            Object cmp = m.invoke(strategy);
            if (cmp instanceof Comparator) {
                List<ITask> copy = new ArrayList<>(src);
                copy.sort((Comparator<? super ITask>) cmp);
                return copy;
            }
        } catch (NoSuchMethodException ignore) {
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

    /**
     * Verifies that {@link SortById} orders tasks by ascending id.
     */
    @Test
    public void sortById_ascending() {
        SortStrategy s = new SortById();
        List<ITask> sorted = apply(s, sample());
        assertEquals(List.of(1, 2, 3, 4, 5), ids(sorted));
    }

    /**
     * Verifies that {@link SortById} is stable for empty and single-element inputs.
     */
    @Test
    public void sortById_emptyAndSingle_areStable() {
        SortStrategy s = new SortById();
        assertTrue(apply(s, List.of()).isEmpty());
        ITask one = new T(7, "x", "d", TaskState.ToDo);
        assertEquals(List.of(one), apply(s, List.of(one)));
    }

    /**
     * Verifies that {@link SortByTitle} sorts titles case-insensitively in lexicographic order.
     */
    @Test
    public void sortByTitle_lexicographic_caseInsensitive() {
        SortStrategy s = new SortByTitle();
        List<ITask> sorted = apply(s, sample());
        List<String> actual = titles(sorted);

        List<String> expected = new ArrayList<>(titles(sample()));
        expected.sort(String.CASE_INSENSITIVE_ORDER);

        assertEquals("titles should be sorted case-insensitively", expected, actual);

        int i = actual.indexOf("alpha"), j = actual.lastIndexOf("alpha");
        assertTrue("alphas should be adjacent", j - i == 1);
    }

    /**
     * Verifies that {@link SortByTitle} is stable for empty and single-element inputs.
     */
    @Test
    public void sortByTitle_emptyAndSingle_areStable() {
        SortStrategy s = new SortByTitle();
        assertTrue(apply(s, List.of()).isEmpty());
        ITask one = new T(9, "Only", "d", TaskState.Completed);
        assertEquals(List.of("Only"), titles(apply(s, List.of(one))));
    }

    /**
     * Verifies that {@link SortByState} orders tasks by lifecycle:
     * ToDo, then InProgress, then Completed.
     */
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

    /**
     * Verifies that {@link SortByState} is stable for empty and single-element inputs.
     */
    @Test
    public void sortByState_emptyAndSingle_areStable() {
        SortStrategy s = new SortByState();
        assertTrue(apply(s, List.of()).isEmpty());
        ITask one = new T(11, "one", "d", TaskState.InProgress);
        assertEquals(List.of(TaskState.InProgress), states(apply(s, List.of(one))));
    }
}
