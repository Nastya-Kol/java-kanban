package manager;

import model.Task;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class ManagersTest {

    @Test
    public void testGetDefaultManager() {
        TaskManager manager = Managers.getDefaultManager();

        assertNotNull("Менеджер задач не равен null", manager);
        assertTrue("менеджер реализует интерфейс TaskManager", manager instanceof TaskManager);

        int taskId = manager.saveTasks(new Task("Task 1", "Description Task 1", NEW));
        Task task = manager.getTaskById(taskId);

        assertNotNull("Задача не null", task);
        assertEquals("Название не совпадает", "Task 1", task.getName());
    }

    @Test
    public void tesGetDefaultHistoryMemory() {
        HistoryManager historyManager = Managers.getDefaultHistoryMemory();

        assertNotNull("Менеджер истории не должен быть равен null", historyManager);
        assertTrue("Менеджер истории реализует интерфейс HistoryManager", historyManager instanceof HistoryManager);

        Task task = new Task("Task 1", "Description Task 1", NEW);
        Task task2 = new Task("Task 2", "Description Task 2", NEW);

        List<Task> history = new ArrayList<>();
        history.add(task);
        history.add(task2);

        assertNotNull("История не должна быть null", history);
        assertEquals("История не должна быть пустой", 2, history.size());
    }
}