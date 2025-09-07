package taskmanagement.domain;

import org.junit.*;
import taskmanagement.domain.filter.Filters;
import taskmanagement.domain.filter.ITaskFilter;

/**
 * FiltersTest
 * -----------
 * Verifies Filters basic predicates and combinator operators (and/or/not/all)
 * against the domain Task model.
 */
public class FiltersTest {

    private Task t1, t2, t3;

    @Before
    public void setUp() {
        t1 = new Task(1, "Write tests", "DAO CRUD", TaskState.ToDo);
        t2 = new Task(2, "Wire UI", "MVVM binding", TaskState.InProgress);
        t3 = new Task(3, "Polish UX", "Dark theme", TaskState.Completed);
    }

    @Test
    public void titleContains_basic() {
        Assert.assertTrue(Filters.titleContains("write").test(t1));
        Assert.assertTrue(Filters.titleContains("WRITE").test(t1));
        Assert.assertFalse(Filters.titleContains("xyz").test(t1));
    }

    @Test
    public void descriptionContains_basic() {
        Assert.assertTrue(Filters.descriptionContains("crud").test(t1));
        Assert.assertTrue(Filters.descriptionContains("BIND").test(t2));
        Assert.assertFalse(Filters.descriptionContains("nope").test(t3));
    }

    @Test
    public void idEquals_basic() {
        Assert.assertTrue(Filters.idEquals(2).test(t2));
        Assert.assertFalse(Filters.idEquals(99).test(t2));
    }

    @Test
    public void stateIs_and_alias() {
        Assert.assertTrue(Filters.stateIs(TaskState.Completed).test(t3));
        Assert.assertTrue(Filters.byState(TaskState.ToDo).test(t1));
        Assert.assertFalse(Filters.byState(TaskState.Completed).test(t1));
    }

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
