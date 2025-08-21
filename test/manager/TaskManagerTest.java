package manager;

import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Epic epic;
    protected Epic epic2;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Task task;
    protected Task task2;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {

        taskManager = createTaskManager();
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 0);

        epic = new Epic("Test Epic Name", "Test Epic Description", TaskStatus.NEW, Duration.ofMinutes(70), baseTime);
        Epic epic2 = new Epic("Test Epic2 Name", "Test Epic2 Description", TaskStatus.NEW, Duration.ofMinutes(30), baseTime.plusHours(5));
        int epicId = taskManager.saveEpics(epic);

        task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(5), baseTime.plusHours(3));
        task2 = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(5), baseTime.plusHours(6));

        epic = taskManager.getEpicById(epicId);

        subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epicId,
                Duration.ofMinutes(5), baseTime);
        subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.NEW, epicId,
                Duration.ofMinutes(5), baseTime.plusHours(1));
    }

    @Test
    void testEpicStatusAllNew() {
        //Все подзадачи со статусом NEW
        taskManager.saveSubtasks(subtask1);
        taskManager.saveSubtasks(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus(),
                "Эпик должен иметь статус NEW, когда все подзадачи NEW");
    }

    @Test
    void testEpicStatusAllDone() {
        //Все подзадачи со статусом DONE
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        taskManager.saveSubtasks(subtask1);
        taskManager.saveSubtasks(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus(),
                "Эпик должен иметь статус DONE, когда все подзадачи DONE");
    }

    @Test
    void testEpicStatusNewAndDone() {
        //Подзадачи со статусами NEW и DONE
        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.DONE);

        taskManager.saveSubtasks(subtask1);
        taskManager.saveSubtasks(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(),
                "Эпик должен иметь статус IN_PROGRESS, когда есть подзадачи NEW и DONE");
    }

    @Test
    void testEpicStatusInProgress() {
        //Подзадачи со статусом IN_PROGRESS
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.saveSubtasks(subtask1);
        taskManager.saveSubtasks(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(),
                "Эпик должен иметь статус IN_PROGRESS, когда есть подзадачи IN_PROGRESS");
    }

    @Test
    void testSubtaskHasEpic() {
        int subtaskId = taskManager.saveSubtasks(subtask1);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача должна быть сохранена");
        assertEquals(epic.getId(), savedSubtask.getEpic(),
                "Подзадача должна быть связана с эпиком");
    }

    @Test
    void testTimeOverlapDetection() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW, Duration.ofHours(2),
                LocalDateTime.of(2025, 1, 1, 10, 0));

        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW, Duration.ofHours(2),
                LocalDateTime.of(2025, 1, 1, 11, 0));

        taskManager.saveTasks(task1);

        assertThrows(ManagerSaveException.class, () -> taskManager.saveTasks(task2),
                "Должно быть исключение при пересечении времени задач");
    }

    @Test
    void testNoTimeOverlap() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW, Duration.ofHours(1),
                LocalDateTime.of(2025, 1, 1, 10, 0));

        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW, Duration.ofHours(1),
                LocalDateTime.of(2025, 1, 1, 12, 0));

        taskManager.saveTasks(task1);

        assertDoesNotThrow(() -> taskManager.saveTasks(task2),
                "Не должно быть исключения при отсутствии пересечения времени");
    }

    @Test
    void testPrioritizedTasksOrder() {
        Task earlyTask = new Task("Early", "Desc", TaskStatus.NEW, Duration.ofHours(1),
                LocalDateTime.of(2025, 1, 1, 9, 0));

        Task lateTask = new Task("Late", "Desc", TaskStatus.NEW, Duration.ofHours(1),
                LocalDateTime.of(2025, 1, 1, 11, 0));

        taskManager.saveTasks(lateTask);
        taskManager.saveTasks(earlyTask);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(earlyTask.getId(), prioritized.get(0).getId(),
                "Задачи должны быть отсортированы по времени начала");
    }

    @Test
    void addNewTask() {

        final int taskId = taskManager.saveTasks(task);
        final int taskId2 = taskManager.saveTasks(task2);

        final Task savedTask = taskManager.getTaskById(taskId);
        final Task savedTask2 = taskManager.getTaskById(taskId2);

        assertNotNull("Задача не найдена.", savedTask);
        assertEquals("Задачи не совпадают.", task, savedTask);

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull("Задачи не возвращаются.", tasks);
        assertEquals("Неверное количество задач.", 2, tasks.size());
        assertEquals("Задачи не совпадают.", task, tasks.get(0));
    }

    @Test
    void addNewEpic() {

        final int epicId = taskManager.saveEpics(epic);
        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull("Эпик не найден.", savedEpic);
        assertEquals("Эпики  не совпадают.", epic, savedEpic);

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull("Эпики не возвращаются.", epics);
        assertEquals("Неверное количество Эпиков.", 2, epics.size());
        assertEquals("Эпики не совпадают.", epic, epics.get(0));
    }

    @Test
    void addNewSubtask() {

        final int subtaskId = taskManager.saveSubtasks(subtask1);
        final int subtaskId2 = taskManager.saveSubtasks(subtask2);

        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        final Subtask savedSubtask2 = taskManager.getSubtaskById(subtaskId2);

        assertNotNull("Подзадача не найдена.", savedSubtask);
        assertEquals("Подзадачи не совпадают.", subtask1, savedSubtask);

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull("Подзадачи не возвращаются.", subtasks);
        assertEquals("Неверное количество Подзадач.", 2, subtasks.size());
        assertEquals("Подзадачи не совпадают.", subtask1, subtasks.get(0));
    }

    @Test
    void findNotExistentTask() {
        Task task = taskManager.getTaskById(34);

        Assertions.assertNull(task, "Задача не существует, должна быть null");
    }

    @Test
    void shouldClearHistoryWhenDeleteAllTasks() {
        TaskManager taskmanager = new InMemoryTaskManager();
        int id1 = taskmanager.saveTasks(task);
        int id2 = taskmanager.saveTasks(task2);

        taskmanager.getTaskById(id1);
        taskmanager.getTaskById(id2);
        taskmanager.deleteAllTasks();

        assertTrue(taskmanager.getHistory().isEmpty());
    }

    @Test
    void shouldClearEpicsAndSubtasksHistoryWhenDeleteAllEpics() {
        TaskManager taskmanager = new InMemoryTaskManager();
        int epicId = taskmanager.saveEpics(epic);
        int subId = taskmanager.saveSubtasks(subtask1);

        taskmanager.getEpicById(epicId);
        taskmanager.getSubtaskById(subId);
        taskmanager.deleteAllEpics();

        assertTrue(taskmanager.getHistory().isEmpty());
    }
}
