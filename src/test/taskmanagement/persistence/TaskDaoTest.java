package taskmanagement.persistence;

import org.junit.*; // JUnit 4
import org.junit.Test;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import taskmanagement.domain.ITask;
import taskmanagement.domain.Task;
import taskmanagement.domain.TaskState;
import taskmanagement.persistence.derby.EmbeddedDerbyTasksDAO;

/**
 * TaskDaoTest (JUnit 4)
 * ---------------------
 * CRUD tests for EmbeddedDerbyTasksDAO.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // optional: run by method name
public class TaskDaoTest {

    private EmbeddedDerbyTasksDAO dao;

    @Before
    public void setUp() throws TasksDAOException {
        dao = EmbeddedDerbyTasksDAO.getInstance();
        dao.deleteTasks();
    }

    @AfterClass
    public static void afterAll() {
        EmbeddedDerbyTasksDAO.getInstance().shutdown();
    }

    /** 1) addTask + getTasks returns inserted task */
    @Test
    public void test01_add_and_list() throws TasksDAOException {
        Task t = new Task(0, "Write tests", "DAO CRUD", TaskState.ToDo);
        dao.addTask(t);

        Assert.assertTrue("Expected DAO to assign id > 0", t.getId() > 0);

        ITask[] all = dao.getTasks();
        Assert.assertEquals(1, all.length);
        Assert.assertEquals("Write tests", all[0].getTitle());
        Assert.assertEquals(TaskState.ToDo, all[0].getState());
    }

    /** 2) getTask by id and not-found behavior */
    @Test
    public void test02_get_by_id_and_not_found() throws TasksDAOException {
        Task t = new Task(0, "A", "desc", TaskState.InProgress);
        dao.addTask(t);
        int id = t.getId();

        ITask fromDb = dao.getTask(id);
        Assert.assertEquals("A", fromDb.getTitle());
        Assert.assertEquals(TaskState.InProgress, fromDb.getState());

        Assert.assertThrows(TasksDAOException.class, () -> dao.getTask(999_999));
    }

    /** 3) addTask with explicit id and duplicate key error */
    @Test
    public void test03_add_with_explicit_id_and_duplicate() throws TasksDAOException {
        Task t1 = new Task(42, "X", "first", TaskState.ToDo);
        dao.addTask(t1);
        Assert.assertEquals(42, t1.getId());

        Task t2 = new Task(42, "Y", "dup", TaskState.ToDo);
        Assert.assertThrows(TasksDAOException.class, () -> dao.addTask(t2));
    }

    /** 4) updateTask modifies existing and fails when missing */
    @Test
    public void test04_update_existing_and_missing() throws TasksDAOException {
        Task t = new Task(0, "Before", "desc", TaskState.ToDo);
        dao.addTask(t);
        int id = t.getId();

        Task updated = new Task(id, "After", "desc2", TaskState.Completed);
        dao.updateTask(updated);

        ITask fromDb = dao.getTask(id);
        Assert.assertEquals("After", fromDb.getTitle());
        Assert.assertEquals("desc2", fromDb.getDescription());
        Assert.assertEquals(TaskState.Completed, fromDb.getState());

        Task missing = new Task(123456, "Nope", "NA", TaskState.ToDo);
        Assert.assertThrows(TasksDAOException.class, () -> dao.updateTask(missing));
    }

    /** 5) deleteTask removes row and fails when not found */
    @Test
    public void test05_delete_single_and_missing() throws TasksDAOException {
        Task t = new Task(0, "To delete", "d", TaskState.ToDo);
        dao.addTask(t);
        int id = t.getId();

        dao.deleteTask(id);
        Assert.assertThrows(TasksDAOException.class, () -> dao.getTask(id));
        Assert.assertThrows(TasksDAOException.class, () -> dao.deleteTask(id));
    }

    /** 6) deleteTasks clears table */
    @Test
    public void test06_delete_all() throws TasksDAOException {
        dao.addTask(new Task(0, "A", "1", TaskState.ToDo));
        dao.addTask(new Task(0, "B", "2", TaskState.Completed));
        Assert.assertTrue(dao.getTasks().length >= 2);

        dao.deleteTasks();
        Assert.assertEquals(0, dao.getTasks().length);
    }
}
