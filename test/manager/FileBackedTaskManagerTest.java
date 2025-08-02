package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import model.Task;
import model.Epic;
import model.Subtask;
import model.TaskStatus;

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
    void shouldSaveAndLoadEmptyManager() {
        manager.save();
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
        Subtask subtask = new Subtask(3, "Subtask 1", "Description 3", TaskStatus.NEW, 2);

        manager.saveTasks(task);
        manager.saveEpics(epic);
        manager.saveSubtasks(subtask);

        // Явно сохраняем изменения в файл
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем загруженные задачи
        List<Task> tasks = loadedManager.getTasks();
        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals(1, epics.size(), "Неверное количество эпиков");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач");

        assertEquals("Task 1", tasks.get(0).getName(), "Неверное название задачи");
        assertEquals("Epic 1", epics.get(0).getName(), "Неверное название эпика");
        assertEquals("Subtask 1", subtasks.get(0).getName(), "Неверное название подзадачи");

        // Проверяем связь подзадачи с эпиком
        assertEquals(2, subtasks.get(0).getEpic(), "Неверный epicId у подзадачи");

    }

    @Test
    void shouldCorrectlySaveAndLoadTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        manager.saveTasks(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loaded.getTaskById(1);

        assertNotNull(loadedTask);
        assertEquals("Task 1", loadedTask.getName());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());
    }

}