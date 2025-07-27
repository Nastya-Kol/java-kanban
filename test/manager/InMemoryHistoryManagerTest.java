package manager;

import model.Task;
import model.TaskStatus;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class InMemoryHistoryManagerTest {

    InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void addTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        historyManager.addTask(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull("История не пустая.", history);
        assertEquals("История не пустая.", 1, history.size());
    }

    @Test
    public void shouldAddTasksToHistory() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setId(2);

        historyManager.addTask(task1);
        historyManager.addTask(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    public void shouldNotDuplicateTasksInHistory() {
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.addTask(task);
        historyManager.addTask(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    public void shouldRemoveTasksFromHistory() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setId(2);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    public void shouldKeepOrderAfterRemoval() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description", TaskStatus.NEW);
        task3.setId(3);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    public void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }
}