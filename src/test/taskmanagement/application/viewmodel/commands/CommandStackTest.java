package taskmanagement.application.viewmodel.commands;

import org.junit.Before;
import org.junit.Test;
import taskmanagement.domain.ITask;
import taskmanagement.domain.TaskState;
import taskmanagement.domain.exceptions.ValidationException;
import taskmanagement.persistence.ITasksDAO;
import taskmanagement.persistence.TasksDAOException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * CommandStackTest
 * ----------------
 * Unit tests for the Command pattern stack (execute/undo/redo) with the concrete
 * commands: AddTaskCommand, UpdateTaskCommand, DeleteTaskCommand, MarkStateCommand.
 *
 * Notes:
 *  • Uses an in-memory FakeDAO (no Derby) to keep tests fast and deterministic.
 *  • Uses a local MutableTask test-double that implements ITask (with setters).
 *  • Verifies idempotent redo behavior and state restoration on undo.
 */
public final class CommandStackTest {

    // -------- Test doubles --------

    /**
     * Minimal mutable task used for tests only.
     * Provides setters so FakeDAO/commands can reflect assigned ids and changes.
     */
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

        /** Copy constructor */
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
            // Not used in these tests
        }
    }

    /**
     * In-memory DAO fake:
     *  • addTask: assigns id if <= 0, otherwise preserves given id (so redo after undo keeps same id).
     *  • updateTask: replaces by id.
     *  • deleteTask: removes by id.
     */
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
                mt.setId(id); // reflect effective id into the task instance
            } else {
                // if id already exists, treat as conflict
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

        // Helpers for assertions
        boolean exists(int id) { return store.containsKey(id); }
        Optional<MutableTask> find(int id) {
            ITask t = store.get(id);
            return Optional.ofNullable(t).map(it -> new MutableTask((MutableTask) it));
        }
        int size() { return store.size(); }
    }

    // -------- Fixtures --------

    private FakeDAO dao;
    private CommandStack stack;

    @Before
    public void setUp() {
        dao = new FakeDAO();
        stack = new CommandStack();
    }

    // -------- Tests --------

    /**
     * Verifies AddTaskCommand: execute adds a row and reflects id; undo deletes; redo adds back (same id).
     */
    @Test
    public void add_execute_undo_redo() throws Exception {
        MutableTask t = new MutableTask(0, "A", "desc", TaskState.ToDo);
        AddTaskCommand add = new AddTaskCommand(dao, t);

        // execute
        stack.execute(add);
        assertTrue("id should be assigned (>0)", t.getId() > 0);
        int assigned = t.getId();
        assertEquals(1, dao.size());
        assertTrue(dao.exists(assigned));

        // undo
        stack.undo();
        assertEquals(0, dao.size());
        assertFalse(dao.exists(assigned));

        // redo
        stack.redo();
        assertEquals(1, dao.size());
        assertTrue("redo should reinsert same id", dao.exists(assigned));
    }

    /**
     * Verifies UpdateTaskCommand: execute applies 'after'; undo restores 'before'; redo applies 'after' again.
     */
    @Test
    public void update_execute_undo_redo() throws Exception {
        // seed initial row
        MutableTask before = new MutableTask(0, "Title-1", "D", TaskState.ToDo);
        new AddTaskCommand(dao, before).execute();
        int id = before.getId();

        // after snapshot
        MutableTask after = new MutableTask(id, "Title-2", "D", TaskState.ToDo);
        UpdateTaskCommand upd = new UpdateTaskCommand(dao, copy(before), copy(after));

        // through stack
        stack.execute(upd);
        assertEquals("Title-2", dao.getTask(id).getTitle());

        stack.undo();
        assertEquals("Title-1", dao.getTask(id).getTitle());

        stack.redo();
        assertEquals("Title-2", dao.getTask(id).getTitle());
    }

    /**
     * Verifies DeleteTaskCommand: execute removes; undo re-adds same snapshot (same id); redo removes again.
     */
    @Test
    public void delete_execute_undo_redo() throws Exception {
        // seed
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
     * Verifies MarkStateCommand: execute changes state; undo restores; redo changes again.
     */
    @Test
    public void mark_state_execute_undo_redo() throws Exception {
        // seed
        MutableTask t = new MutableTask(0, "S", "D", TaskState.ToDo);
        new AddTaskCommand(dao, t).execute();
        int id = t.getId();
        assertEquals(TaskState.ToDo, dao.getTask(id).getState());

        // factory that clones with new state
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

    // -------- Helpers --------

    private static MutableTask copy(MutableTask mt) {
        return new MutableTask(mt.getId(), mt.getTitle(), mt.getDescription(), mt.getState());
    }
}
