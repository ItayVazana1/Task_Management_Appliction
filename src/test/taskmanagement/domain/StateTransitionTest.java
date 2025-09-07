package taskmanagement.domain;

import org.junit.Assert;
import org.junit.Test;
import taskmanagement.domain.exceptions.ValidationException;

/**
 * StateTransitionTest
 * -------------------
 * Verifies the Task lifecycle transitions (State pattern).
 * <ul>
 *   <li>Allowed: ToDo -> InProgress -> Completed</li>
 *   <li>Forbidden: backward transitions (InProgress -> ToDo, Completed -> InProgress/ToDo)</li>
 *   <li>Idempotent: setting the same state twice is allowed</li>
 *   <li>Validation: null state throws ValidationException</li>
 * </ul>
 */
public class StateTransitionTest {

    private static Task newTaskIn(TaskState state) {
        return new Task(200, "Sample", "Lifecycle test", state);
    }

    // ---- Allowed forward transitions ----

    @Test
    public void allow_todo_to_inprogress() throws Exception {
        Task task = newTaskIn(TaskState.ToDo);
        task.setState(TaskState.InProgress);
        Assert.assertEquals(TaskState.InProgress, task.getState());
    }

    @Test
    public void allow_inprogress_to_completed() throws Exception {
        Task task = newTaskIn(TaskState.InProgress);
        task.setState(TaskState.Completed);
        Assert.assertEquals(TaskState.Completed, task.getState());
    }

    @Test
    public void allow_todo_to_completed_via_two_steps() throws Exception {
        Task task = newTaskIn(TaskState.ToDo);
        task.setState(TaskState.InProgress);
        task.setState(TaskState.Completed);
        Assert.assertEquals(TaskState.Completed, task.getState());
    }

    // ---- Forbidden backward transitions ----

    @Test(expected = ValidationException.class)
    public void forbid_inprogress_back_to_todo() throws Exception {
        Task task = newTaskIn(TaskState.InProgress);
        task.setState(TaskState.ToDo);
    }

    @Test(expected = ValidationException.class)
    public void forbid_completed_back_to_inprogress() throws Exception {
        Task task = newTaskIn(TaskState.Completed);
        task.setState(TaskState.InProgress);
    }

    @Test(expected = ValidationException.class)
    public void forbid_completed_back_to_todo() throws Exception {
        Task task = newTaskIn(TaskState.Completed);
        task.setState(TaskState.ToDo);
    }

    // ---- Idempotent transitions ----

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

    // ---- Null validation ----

    @Test(expected = ValidationException.class)
    public void null_state_is_invalid() throws Exception {
        Task task = newTaskIn(TaskState.ToDo);
        task.setState(null);
    }
}
