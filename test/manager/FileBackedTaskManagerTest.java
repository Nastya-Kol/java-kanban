package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import model.Task;
import model.Epic;
import model.Subtask;
import model.TaskStatus;

import java.io.*;
import java.util.List;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;
    private FileBackedTaskManager manager;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = Files.createTempFile("test", ".csv").toFile();
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать тестовый файл", e);
        }
    }

    // Сохранение и загрузка пустого файла
    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("");
        }
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    // Сохранение нескольких задач
    @Test
    void shouldSaveMultipleTasks() throws IOException {

        manager = createTaskManager();
        manager.saveEpics(epic);
        manager.saveTasks(task);
        manager.saveSubtasks(subtask1);

        // Проверяем содержимое файла
        String content = Files.readString(tempFile.toPath());
        String[] lines = content.split("\n");

        assertEquals(4, lines.length); // Заголовок + 3 задачи
        assertTrue(content.contains("TASK"));
        assertTrue(content.contains("EPIC"));
        assertTrue(content.contains("SUBTASK"));
    }

    // Загрузка нескольких задач
    @Test
    void shouldLoadMultipleTasks() {

        manager = createTaskManager();
        int epicId = manager.saveEpics(epic);
        int subId1 = manager.saveSubtasks(subtask1);
        int subId2 = manager.saveSubtasks(subtask2);
        int taskId = manager.saveTasks(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        //Проверяем задачи
        Task loadedTask = loadedManager.getTaskById(taskId);
        assertNotNull(loadedTask, "Задача не загрузилась");
        assertEquals(task.getId(), loadedTask.getId(), "ID задачи не совпадает");
        assertEquals(task.getName(), loadedTask.getName(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Статус задачи не совпадает");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала задачи не совпадает");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Продолжительность задачи не совпадает");
        assertEquals(task.getEndTime(), loadedTask.getEndTime(), "Время окончания задачи не совпадает");

        // Проверяем эпики
        Epic loadedEpic = loadedManager.getEpicById(epicId);
        assertNotNull(loadedEpic, "Эпик не загрузился");
        assertEquals(epic.getId(), loadedEpic.getId(), "ID эпика не совпадает");
        assertEquals(epic.getName(), loadedEpic.getName(), "Название эпика не совпадает");
        assertEquals(epic.getDescription(), loadedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(epic.getStatus(), loadedEpic.getStatus(), "Статус эпика не совпадает");
        assertEquals(epic.getStartTime(), loadedEpic.getStartTime(), "Время начала эпика не совпадает");
        assertEquals(epic.getDuration(), loadedEpic.getDuration(), "Продолжительность эпика не совпадает");
        assertEquals(epic.getEndTime(), loadedEpic.getEndTime(), "Время окончания эпика не совпадает");

        // Проверяем подзадачи
        Subtask loadedSubtask1 = loadedManager.getSubtaskById(subId1);
        assertNotNull(loadedSubtask1, "Подзадача 1 не загрузилась");
        assertEquals(subtask1.getId(), loadedSubtask1.getId(), "ID подзадачи 1 не совпадает");
        assertEquals(subtask1.getName(), loadedSubtask1.getName(), "Название подзадачи 1 не совпадает");
        assertEquals(subtask1.getDescription(), loadedSubtask1.getDescription(), "Описание подзадачи 1 не совпадает");
        assertEquals(subtask1.getStatus(), loadedSubtask1.getStatus(), "Статус подзадачи 1 не совпадает");
        assertEquals(subtask1.getEpic(), loadedSubtask1.getEpic(), "EpicID подзадачи 1 не совпадает");
        assertEquals(subtask1.getStartTime(), loadedSubtask1.getStartTime(), "Время начала подзадачи 1 не совпадает");
        assertEquals(subtask1.getDuration(), loadedSubtask1.getDuration(), "Продолжительность подзадачи 1 не совпадает");
        assertEquals(subtask1.getEndTime(), loadedSubtask1.getEndTime(), "Время окончания подзадачи 1 не совпадает");

        Subtask loadedSubtask2 = loadedManager.getSubtaskById(subId2);
        assertNotNull(loadedSubtask2, "Подзадача 2 не загрузилась");
        assertEquals(subtask2.getId(), loadedSubtask2.getId(), "ID подзадачи 2 не совпадает");
        assertEquals(subtask2.getName(), loadedSubtask2.getName(), "Название подзадачи 2 не совпадает");
        assertEquals(subtask2.getDescription(), loadedSubtask2.getDescription(), "Описание подзадачи 2 не совпадает");
        assertEquals(subtask2.getStatus(), loadedSubtask2.getStatus(), "Статус подзадачи 2 не совпадает");
        assertEquals(subtask2.getEpic(), loadedSubtask2.getEpic(), "EpicID подзадачи 2 не совпадает");
        assertEquals(subtask2.getStartTime(), loadedSubtask2.getStartTime(), "Время начала подзадачи 2 не совпадает");
        assertEquals(subtask2.getDuration(), loadedSubtask2.getDuration(), "Продолжительность подзадачи 2 не совпадает");
        assertEquals(subtask2.getEndTime(), loadedSubtask2.getEndTime(), "Время окончания подзадачи 2 не совпадает");

        // Проверяем связи эпиков и подзадач
        assertEquals(2, loadedEpic.getSubTaskIds().size(), "Неверное количество подзадач у эпика");
        assertTrue(loadedEpic.getSubTaskIds().contains(2), "Эпик не содержит подзадачу 1");
        assertTrue(loadedEpic.getSubTaskIds().contains(3), "Эпик не содержит подзадачу 2");

        // Проверяем статус эпика (должен быть IN_PROGRESS, так как есть NEW и DONE подзадачи)
        assertEquals(TaskStatus.NEW, loadedEpic.getStatus(), "Статус эпика неверный");

        // Проверяем общее количество задач
        assertEquals(1, loadedManager.getTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getEpics().size(), "Неверное количество эпиков");

        // Проверяем отсортированный список prioritizedTasks
        List<Task> loadedPrioritized = loadedManager.getPrioritizedTasks();
        List<Task> originalPrioritized = manager.getPrioritizedTasks();

        assertEquals(originalPrioritized.size(), loadedPrioritized.size(),
                "Размер отсортированного списка не совпадает");

        // Проверяем, что список правильно отсортирован по времени начала
        for (int i = 0; i < loadedPrioritized.size() - 1; i++) {
            assertTrue(loadedPrioritized.get(i).getStartTime()
                            .isBefore(loadedPrioritized.get(i + 1).getStartTime()),
                    "Список не отсортирован по времени начала");
        }

        // Проверяем, что все задачи присутствуют в отсортированном списке
        assertTrue(loadedPrioritized.contains(loadedTask), "Задача отсутствует в отсортированном списке");
        assertTrue(loadedPrioritized.contains(loadedSubtask1), "Подзадача 1 отсутствует в отсортированном списке");
        assertTrue(loadedPrioritized.contains(loadedSubtask2), "Подзадача 2 отсутствует в отсортированном списке");

        // Проверяем, что эпик НЕ в отсортированном списке
        assertFalse(loadedPrioritized.contains(loadedEpic), "Эпик не должен быть в отсортированном списке");
    }
}