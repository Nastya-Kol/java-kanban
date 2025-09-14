package manager;

import com.google.gson.Gson;
import model.Epic;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    protected TaskManager taskManager = new InMemoryTaskManager();
    protected HttpTaskServer taskServer;
    Epic epic = new Epic("Test Epic Name", "Test Epic Description", TaskStatus.NEW
            , Duration.ofMinutes(70), LocalDateTime.of(2025, 1, 1, 10, 0));
    int epicId = taskManager.saveEpics(epic);
    protected Gson gson;

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {

        gson = HttpTaskServer.getGson();
        try {
            taskServer = new HttpTaskServer(taskManager, gson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks(epicId);
        taskManager.deleteAllEpics();
        taskServer.start();

    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Description task 2",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Description task 2",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        int taskId = taskManager.saveTasks(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(0, taskManager.getTasks().size());
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Description task 2",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        taskManager.saveTasks(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test 2", tasks[0].getName());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Description task 2",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        int taskId = taskManager.saveTasks(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(taskId, retrievedTask.getId());
        assertEquals("Test 2", retrievedTask.getName());
    }
}