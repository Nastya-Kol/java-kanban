package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Epic epic;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Task task;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        epic = new Epic("Test Epic Name", "Test Epic Description", TaskStatus.NEW, Duration.ofMinutes(70), baseTime);
        int epicId = taskManager.saveEpics(epic);
        task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                Duration.ofMinutes(5), baseTime.plusHours(3));

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
}
