package taskmanagement.application.viewmodel.commands;

import org.junit.Before;
import org.junit.Test;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * JUnit 4 tests for the command stack (execute/undo/redo) used in the
 * tasks management application. Verifies behavior of {@code AddTaskCommand},
 * {@code UpdateTaskCommand}, {@code DeleteTaskCommand}, and {@code MarkStateCommand}
 * against an in-memory fake DAO using a minimal mutable task double.
 */
public final class CommandStackTest {

    private static final class MutableTask implements ITask {
        private int id;
        private String title;
        private String description;
        private TaskState state;

        MutableTask(int id, String title, String description, TaskState state) {
            this.id = id;
            this.title = Objects.requireNonNull(title, "title");
            this.description = Objects.requireNonNull(description, "description");
            this.state = Objects.requireNonNull(state, "state");
        }

        MutableTask(MutableTask other) {
            this(other.id, other.title, other.description, other.state);
        }

        void setId(int id) { this.id = id; }
        void setTitle(String t) { this.title = Objects.requireNonNull(t); }
        void setDescription(String d) { this.description = Objects.requireNonNull(d); }
        void setState(TaskState s) { this.state = Objects.requireNonNull(s); }

        @Override public int getId() { return id; }
        @Override public String getTitle() { return title; }
        @Override public String getDescription() { return description; }
        @Override public TaskState getState() { return state; }

        @Override
        public void accept(taskmanagement.domain.visitor.TaskVisitor visitor) {
            // Intentionally unused in these tests.
        }
    }

    private static final class FakeDAO implements ITasksDAO {
        private final Map<Integer, ITask> store = new LinkedHashMap<>();
        private final AtomicInteger seq = new AtomicInteger(1);

        @Override
        public ITask[] getTasks() { return store.values().toArray(new ITask[0]); }

        @Override
        public ITask getTask(int id) throws TasksDAOException {
            ITask t = store.get(id);
            if (t == null) throw new TasksDAOException("Not found: " + id);
            return t;
        }

        @Override
        public void addTask(ITask task) throws TasksDAOException {
            if (!(task instanceof MutableTask mt)) {
                throw new TasksDAOException("Test expects MutableTask for id reflection");
            }
            int id = mt.getId();
            if (id <= 0) {
                id = seq.getAndIncrement();
                mt.setId(id);
            } else {
                if (store.containsKey(id)) {
                    throw new TasksDAOException("Duplicate id: " + id);
                }
            }
            store.put(id, cloneForSafety(mt));
        }

        @Override
        public void updateTask(ITask task) throws TasksDAOException {
            if (!(task instanceof MutableTask mt)) {
                throw new TasksDAOException("Test expects MutableTask");
            }
            int id = mt.getId();
            if (!store.containsKey(id)) throw new TasksDAOException("Cannot update missing id: " + id);
            store.put(id, cloneForSafety(mt));
        }

        @Override
        public void deleteTasks() { store.clear(); }

        @Override
        public void deleteTask(int id) throws TasksDAOException {
            if (store.remove(id) == null) throw new TasksDAOException("Nothing to delete for id: " + id);
        }

        private static MutableTask cloneForSafety(MutableTask mt) {
            return new MutableTask(mt.getId(), mt.getTitle(), mt.getDescription(), mt.getState());
        }

        boolean exists(int id) { return store.containsKey(id); }
        Optional<MutableTask> find(int id) {
            ITask t = store.get(id);
            return Optional.ofNullable(t).map(it -> new MutableTask((MutableTask) it));
        }
        int size() { return store.size(); }
    }

    private FakeDAO dao;
    private CommandStack stack;

    /**
     * Initializes the in-memory DAO and command stack before each test.
     */
    @Before
    public void setUp() {
        dao = new FakeDAO();
        stack = new CommandStack();
    }

    /**
     * Ensures {@link AddTaskCommand} adds a row and reflects an assigned id,
     * then validates that undo deletes the row and redo reinserts it with the same id.
     *
     * @throws Exception if the command execution fails unexpectedly
     */
    @Test
    public void add_execute_undo_redo() throws Exception {
        MutableTask t = new MutableTask(0, "A", "desc", TaskState.ToDo);
        AddTaskCommand add = new AddTaskCommand(dao, t);

        stack.execute(add);
        assertTrue("id should be assigned (>0)", t.getId() > 0);
        int assigned = t.getId();
        assertEquals(1, dao.size());
        assertTrue(dao.exists(assigned));

        stack.undo();
        assertEquals(0, dao.size());
        assertFalse(dao.exists(assigned));

        stack.redo();
        assertEquals(1, dao.size());
        assertTrue("redo should reinsert same id", dao.exists(assigned));
    }

    /**
     * Ensures {@link UpdateTaskCommand} applies the updated snapshot on execute,
     * restores the original snapshot on undo, and reapplies the update on redo.
     *
     * @throws Exception if the command execution fails unexpectedly
     */
    @Test
    public void update_execute_undo_redo() throws Exception {
        MutableTask before = new MutableTask(0, "Title-1", "D", TaskState.ToDo);
        new AddTaskCommand(dao, before).execute();
        int id = before.getId();

        MutableTask after = new MutableTask(id, "Title-2", "D", TaskState.ToDo);
        UpdateTaskCommand upd = new UpdateTaskCommand(dao, copy(before), copy(after));

        stack.execute(upd);
        assertEquals("Title-2", dao.getTask(id).getTitle());

        stack.undo();
        assertEquals("Title-1", dao.getTask(id).getTitle());

        stack.redo();
        assertEquals("Title-2", dao.getTask(id).getTitle());
    }

    /**
     * Ensures {@link DeleteTaskCommand} removes a row on execute, restores it
     * with the same id on undo, and removes it again on redo.
     *
     * @throws Exception if the command execution fails unexpectedly
     */
    @Test
    public void delete_execute_undo_redo() throws Exception {
        MutableTask toDelete = new MutableTask(0, "X", "D", TaskState.ToDo);
        new AddTaskCommand(dao, toDelete).execute();
        int id = toDelete.getId();
        assertTrue(dao.exists(id));

        DeleteTaskCommand del = new DeleteTaskCommand(dao, copy(toDelete));

        stack.execute(del);
        assertFalse(dao.exists(id));

        stack.undo();
        assertTrue("undo should bring task back", dao.exists(id));

        stack.redo();
        assertFalse(dao.exists(id));
    }

    /**
     * Ensures {@link MarkStateCommand} changes state on execute, restores the
     * previous state on undo, and reapplies the new state on redo.
     *
     * @throws Exception if the command execution fails unexpectedly
     */
    @Test
    public void mark_state_execute_undo_redo() throws Exception {
        MutableTask t = new MutableTask(0, "S", "D", TaskState.ToDo);
        new AddTaskCommand(dao, t).execute();
        int id = t.getId();
        assertEquals(TaskState.ToDo, dao.getTask(id).getState());

        MarkStateCommand.TaskFactory factory = (src, newState) -> {
            MutableTask srcMt = (MutableTask) src;
            MutableTask copy = new MutableTask(srcMt);
            copy.setState(newState);
            return copy;
        };

        MarkStateCommand mark = new MarkStateCommand(dao, copy(t), TaskState.InProgress, factory);

        stack.execute(mark);
        assertEquals(TaskState.InProgress, dao.getTask(id).getState());

        stack.undo();
        assertEquals(TaskState.ToDo, dao.getTask(id).getState());

        stack.redo();
        assertEquals(TaskState.InProgress, dao.getTask(id).getState());
    }

    private static MutableTask copy(MutableTask mt) {
        return new MutableTask(mt.getId(), mt.getTitle(), mt.getDescription(), mt.getState());
    }
}
