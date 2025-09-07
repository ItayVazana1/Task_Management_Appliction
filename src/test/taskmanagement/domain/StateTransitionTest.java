package taskmanagement.domain;

import org.junit.Assert;
import org.junit.Test;
import taskmanagement.domain.exceptions.ValidationException;

/**
 * JUnit 4 tests verifying the {@link Task} lifecycle transitions (State pattern).
 * <p>Allowed transitions: ToDo → InProgress → Completed.</p>
 * <p>Forbidden transitions: backward moves (InProgress → ToDo, Completed → InProgress/ToDo).</p>
 * <p>Idempotency: setting the same state twice is allowed.</p>
 * <p>Validation: setting {@code null} state throws {@link ValidationException}.</p>
 */
public class StateTransitionTest {

    private static Task newTaskIn(TaskState state) {
        return new Task(200, "Sample", "Lifecycle test", state);
    }

    /**
     * Allows transition from {@link TaskState#ToDo} to {@link TaskState#InProgress}.
     *
     * @throws Exception if test execution fails unexpectedly
     */
    @Test
    public void allow_todo_to_inprogress() throws Exception {
        Task task = newTaskIn(TaskState.ToDo);
        task.setState(TaskState.InProgress);
        Assert.assertEquals(TaskState.InProgress, task.getState());
    }

    /**
     * Allows transition from {@link TaskState#InProgress} to {@link TaskState#Completed}.
     *
     * @throws Exception if test execution fails unexpectedly
     */
    @Test
    public void allow_inprogress_to_completed() throws Exception {
        Task task = newTaskIn(TaskState.InProgress);
        task.setState(TaskState.Completed);
        Assert.assertEquals(TaskState.Completed, task.getState());
    }

    /**
     * Allows reaching {@link TaskState#Completed} from {@link TaskState#ToDo} via two steps.
     *
     * @throws Exception if test execution fails unexpectedly
     */
    @Test
    public void allow_todo_to_completed_via_two_steps() throws Exception {
        Task task = newTaskIn(TaskState.ToDo);
        task.setState(TaskState.InProgress);
        task.setState(TaskState.Completed);
        Assert.assertEquals(TaskState.Completed, task.getState());
    }

    /**
     * Forbids backward transition from {@link TaskState#InProgress} to {@link TaskState#ToDo}.
     *
     * @throws Exception always thrown by the tested operation
     */
    @Test(expected = ValidationException.class)
    public void forbid_inprogress_back_to_todo() throws Exception {
        Task task = newTaskIn(TaskState.InProgress);
        task.setState(TaskState.ToDo);
    }

    /**
     * Forbids backward transition from {@link TaskState#Completed} to {@link TaskState#InProgress}.
     *
     * @throws Exception always thrown by the tested operation
     */
    @Test(expected = ValidationException.class)
    public void forbid_completed_back_to_inprogress() throws Exception {
        Task task = newTaskIn(TaskState.Completed);
        task.setState(TaskState.InProgress);
    }

    /**
     * Forbids backward transition from {@link TaskState#Completed} to {@link TaskState#ToDo}.
     *
     * @throws Exception always thrown by the tested operation
     */
    @Test(expected = ValidationException.class)
    public void forbid_completed_back_to_todo() throws Exception {
        Task task = newTaskIn(TaskState.Completed);
        task.setState(TaskState.ToDo);
    }

    /**
     * Permits setting the same state value repeatedly (idempotent behavior).
     *
     * @throws Exception if test execution fails unexpectedly
     */
    @Test
    public void idempotent_set_same_state() throws Exception {
        Task t1 = newTaskIn(TaskState.ToDo);
        t1.setState(TaskState.ToDo);
        Assert.assertEquals(TaskState.ToDo, t1.getState());

        Task t2 = newTaskIn(TaskState.InProgress);
        t2.setState(TaskState.InProgress);
        Assert.assertEquals(TaskState.InProgress, t2.getState());

        Task t3 = newTaskIn(TaskState.Completed);
        t3.setState(TaskState.Completed);
        Assert.assertEquals(TaskState.Completed, t3.getState());
    }

    /**
     * Ensures {@code null} state assignment is invalid and results in {@link ValidationException}.
     *
     * @throws Exception always thrown by the tested operation
     */
    @Test(expected = ValidationException.class)
    public void null_state_is_invalid() throws Exception {
        Task task = newTaskIn(TaskState.ToDo);
        task.setState(null);
    }
}
