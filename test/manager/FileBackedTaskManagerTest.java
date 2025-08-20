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

        // Проверяем эпики
        Epic loadedEpic = loadedManager.getEpicById(epicId);
        assertNotNull(loadedEpic, "Эпик не загрузился");
        assertEquals(epic.getId(), loadedEpic.getId(), "ID эпика не совпадает");
        assertEquals(epic.getName(), loadedEpic.getName(), "Название эпика не совпадает");
        assertEquals(epic.getDescription(), loadedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(epic.getStatus(), loadedEpic.getStatus(), "Статус эпика не совпадает");

        // Проверяем подзадачи
        Subtask loadedSubtask1 = loadedManager.getSubtaskById(subId1);
        assertNotNull(loadedSubtask1, "Подзадача 1 не загрузилась");
        assertEquals(subtask1.getId(), loadedSubtask1.getId(), "ID подзадачи 1 не совпадает");
        assertEquals(subtask1.getName(), loadedSubtask1.getName(), "Название подзадачи 1 не совпадает");
        assertEquals(subtask1.getDescription(), loadedSubtask1.getDescription(), "Описание подзадачи 1 не совпадает");
        assertEquals(subtask1.getStatus(), loadedSubtask1.getStatus(), "Статус подзадачи 1 не совпадает");
        assertEquals(subtask1.getEpic(), loadedSubtask1.getEpic(), "EpicID подзадачи 1 не совпадает");

        Subtask loadedSubtask2 = loadedManager.getSubtaskById(subId2);
        assertNotNull(loadedSubtask2, "Подзадача 2 не загрузилась");
        assertEquals(subtask2.getId(), loadedSubtask2.getId(), "ID подзадачи 2 не совпадает");
        assertEquals(subtask2.getName(), loadedSubtask2.getName(), "Название подзадачи 2 не совпадает");
        assertEquals(subtask2.getDescription(), loadedSubtask2.getDescription(), "Описание подзадачи 2 не совпадает");
        assertEquals(subtask2.getStatus(), loadedSubtask2.getStatus(), "Статус подзадачи 2 не совпадает");
        assertEquals(subtask2.getEpic(), loadedSubtask2.getEpic(), "EpicID подзадачи 2 не совпадает");

        // Проверяем связи эпиков и подзадач
        assertEquals(2, loadedEpic.getSubTaskIds().size(), "Неверное количество подзадач у эпика");
        assertTrue(loadedEpic.getSubTaskIds().contains(2), "Эпик не содержит подзадачу 1");
        assertTrue(loadedEpic.getSubTaskIds().contains(3), "Эпик не содержит подзадачу 2");

        // Проверяем статус эпика (должен быть IN_PROGRESS, так как есть NEW и DONE подзадачи)
        assertEquals(TaskStatus.NEW, loadedEpic.getStatus(), "Статус эпика неверный");

        // Проверяем общее количество задач
        assertEquals(1, loadedManager.getTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getEpics().size(), "Неверное количество эпиков");
    }
}