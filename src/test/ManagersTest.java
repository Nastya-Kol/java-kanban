package test;

import manager.Managers;
import manager.TaskManager;
import model.Task;
import org.junit.Test;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class ManagersTest {

    @Test
    public void getDefaultManager() {
        TaskManager manager = Managers.getDefaultManager();

        assertNotNull("Менеджер задач не равен null", manager);
        assertTrue("менеджер реализует интерфейс TaskManager", manager instanceof TaskManager);

        int taskId = manager.saveTasks(new Task ("Task 1", "Description Task 1", NEW));
        Task task = manager.getTaskById(taskId);

        assertNotNull("Задача не null", task);
        assertEquals("Название не совпадает","Task 1", task.getName());
    }
}