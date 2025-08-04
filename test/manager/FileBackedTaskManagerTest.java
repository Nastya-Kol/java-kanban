package manager;

import org.junit.jupiter.api.BeforeEach;
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

public class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
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
        // Создаем задачи
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Description 2", TaskStatus.NEW);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", TaskStatus.NEW, 2);

        // Сохраняем задачи
        manager.saveTasks(task);
        manager.saveEpics(epic);
        manager.saveSubtasks(subtask);

        // Проверяем содержимое файла
        String content = Files.readString(tempFile.toPath());
        String[] lines = content.split("\n");

        assertEquals(4, lines.length); // Заголовок + 3 задачи
        assertTrue(content.contains("1,TASK,Task 1,NEW,Description 1,"));
        assertTrue(content.contains("2,EPIC,Epic 1,NEW,Description 2,"));
        assertTrue(content.contains("3,SUBTASK,Subtask 1,NEW,Description 3,2"));
    }

    // Загрузка нескольких задач
    @Test
    void shouldLoadMultipleTasks() {

        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);
        Epic epic = new Epic(2, "Epic 1", "Description 2", TaskStatus.NEW);
        Subtask subtask1 = new Subtask(3, "Subtask 1", "Description 3", TaskStatus.NEW, 2);
        Subtask subtask2 = new Subtask(4, "Subtask 2", "Description 4", TaskStatus.DONE, 2);

        // 2. Сохраняем в первый менеджер
        manager.saveTasks(task);
        manager.saveEpics(epic);
        manager.saveSubtasks(subtask1);
        manager.saveSubtasks(subtask2);

        // 3. Загружаем во второй менеджер
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // 4. Проверяем задачи
        Task loadedTask = loadedManager.getTaskById(1);
        assertNotNull(loadedTask, "Задача не загрузилась");
        assertEquals(task.getId(), loadedTask.getId(), "ID задачи не совпадает");
        assertEquals(task.getName(), loadedTask.getName(), "Название задачи не совпадает");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Статус задачи не совпадает");

        // 5. Проверяем эпики
        Epic loadedEpic = loadedManager.getEpicById(2);
        assertNotNull(loadedEpic, "Эпик не загрузился");
        assertEquals(epic.getId(), loadedEpic.getId(), "ID эпика не совпадает");
        assertEquals(epic.getName(), loadedEpic.getName(), "Название эпика не совпадает");
        assertEquals(epic.getDescription(), loadedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(epic.getStatus(), loadedEpic.getStatus(), "Статус эпика не совпадает");

        // 6. Проверяем подзадачи
        Subtask loadedSubtask1 = loadedManager.getSubtaskById(3);
        assertNotNull(loadedSubtask1, "Подзадача 1 не загрузилась");
        assertEquals(subtask1.getId(), loadedSubtask1.getId(), "ID подзадачи 1 не совпадает");
        assertEquals(subtask1.getName(), loadedSubtask1.getName(), "Название подзадачи 1 не совпадает");
        assertEquals(subtask1.getDescription(), loadedSubtask1.getDescription(), "Описание подзадачи 1 не совпадает");
        assertEquals(subtask1.getStatus(), loadedSubtask1.getStatus(), "Статус подзадачи 1 не совпадает");
        assertEquals(subtask1.getEpic(), loadedSubtask1.getEpic(), "EpicID подзадачи 1 не совпадает");

        Subtask loadedSubtask2 = loadedManager.getSubtaskById(4);
        assertNotNull(loadedSubtask2, "Подзадача 2 не загрузилась");
        assertEquals(subtask2.getId(), loadedSubtask2.getId(), "ID подзадачи 2 не совпадает");
        assertEquals(subtask2.getName(), loadedSubtask2.getName(), "Название подзадачи 2 не совпадает");
        assertEquals(subtask2.getDescription(), loadedSubtask2.getDescription(), "Описание подзадачи 2 не совпадает");
        assertEquals(subtask2.getStatus(), loadedSubtask2.getStatus(), "Статус подзадачи 2 не совпадает");
        assertEquals(subtask2.getEpic(), loadedSubtask2.getEpic(), "EpicID подзадачи 2 не совпадает");

        // 7. Проверяем связи эпиков и подзадач
        assertEquals(2, loadedEpic.getSubTaskIds().size(), "Неверное количество подзадач у эпика");
        assertTrue(loadedEpic.getSubTaskIds().contains(3), "Эпик не содержит подзадачу 1");
        assertTrue(loadedEpic.getSubTaskIds().contains(4), "Эпик не содержит подзадачу 2");

        // 8. Проверяем статус эпика (должен быть IN_PROGRESS, так как есть NEW и DONE подзадачи)
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus(), "Статус эпика неверный");

        // 9. Проверяем общее количество задач
        assertEquals(1, loadedManager.getTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getEpics().size(), "Неверное количество эпиков");
    }
}