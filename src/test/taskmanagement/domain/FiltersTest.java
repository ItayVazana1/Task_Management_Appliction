package taskmanagement.domain;

import org.junit.*;
import taskmanagement.domain.filter.Filters;
import taskmanagement.domain.filter.ITaskFilter;

/**
 * JUnit 4 tests for the composable task {@link Filters} and the
 * {@link ITaskFilter} combinators (AND/OR/NOT/ALL) against the domain model.
 */
public class FiltersTest {

    private Task t1, t2, t3;

    /**
     * Initializes sample tasks used across the test cases.
     */
    @Before
    public void setUp() {
        t1 = new Task(1, "Write tests", "DAO CRUD", TaskState.ToDo);
        t2 = new Task(2, "Wire UI", "MVVM binding", TaskState.InProgress);
        t3 = new Task(3, "Polish UX", "Dark theme", TaskState.Completed);
    }

    /**
     * Verifies {@link Filters#titleContains(String)} performs a case-insensitive
     * containment match on task titles.
     */
    @Test
    public void titleContains_basic() {
        Assert.assertTrue(Filters.titleContains("write").test(t1));
        Assert.assertTrue(Filters.titleContains("WRITE").test(t1));
        Assert.assertFalse(Filters.titleContains("xyz").test(t1));
    }

    /**
     * Verifies {@link Filters#descriptionContains(String)} performs a
     * case-insensitive containment match on task descriptions.
     */
    @Test
    public void descriptionContains_basic() {
        Assert.assertTrue(Filters.descriptionContains("crud").test(t1));
        Assert.assertTrue(Filters.descriptionContains("BIND").test(t2));
        Assert.assertFalse(Filters.descriptionContains("nope").test(t3));
    }

    /**
     * Verifies {@link Filters#idEquals(int)} matches only the specified id.
     */
    @Test
    public void idEquals_basic() {
        Assert.assertTrue(Filters.idEquals(2).test(t2));
        Assert.assertFalse(Filters.idEquals(99).test(t2));
    }

    /**
     * Verifies {@link Filters#stateIs(TaskState)} and the alias
     * {@link Filters#byState(TaskState)} for state-based matching.
     */
    @Test
    public void stateIs_and_alias() {
        Assert.assertTrue(Filters.stateIs(TaskState.Completed).test(t3));
        Assert.assertTrue(Filters.byState(TaskState.ToDo).test(t1));
        Assert.assertFalse(Filters.byState(TaskState.Completed).test(t1));
    }

    /**
     * Verifies logical composition via {@link ITaskFilter#and(ITaskFilter)},
     * {@link ITaskFilter#or(ITaskFilter)}, {@link ITaskFilter#not(ITaskFilter)},
     * and the match-all predicate {@link Filters#all()}.
     */
    @Test
    public void combinator_and_or_not() {
        ITaskFilter byTitle = Filters.titleContains("ui");
        ITaskFilter inProg  = Filters.stateIs(TaskState.InProgress);

        Assert.assertTrue(byTitle.or(inProg).test(t2));
        Assert.assertFalse(byTitle.and(inProg).test(t1));
        Assert.assertTrue(ITaskFilter.not(byTitle).test(t1));
        Assert.assertTrue(Filters.all().test(t1));
    }
}
