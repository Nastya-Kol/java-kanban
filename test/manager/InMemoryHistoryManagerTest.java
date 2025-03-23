package manager;

import model.Task;
import org.junit.Test;

import java.util.List;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class InMemoryHistoryManagerTest {
    InMemoryTaskManager taskManager = new InMemoryTaskManager();
    InMemoryHistoryManager historyManager = new InMemoryHistoryManager ();

    @Test
    public void addTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        historyManager.addTask(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull("История не пустая.", history);
        assertEquals("История не пустая.", 1, history.size());
    }

}