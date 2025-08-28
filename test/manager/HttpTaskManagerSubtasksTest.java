package manager;

import model.Epic;
import model.Subtask;
import model.TaskStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest extends HttpTaskManagerTasksTest {

    public HttpTaskManagerSubtasksTest() throws IOException {
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        Epic epic = new Epic("Test Epic", "Description epic", TaskStatus.NEW);
        epicId = taskManager.saveEpics(epic);
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Description subtask", TaskStatus.NEW, epicId);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = taskManager.getSubtasks();
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Test Subtask", subtasksFromManager.get(0).getName());
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Description subtask", TaskStatus.NEW, epicId);
        taskManager.saveSubtasks(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length);
        assertEquals("Test Subtask", subtasks[0].getName());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Description subtask", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.saveSubtasks(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtaskId, retrievedSubtask.getId());
        assertEquals("Test Subtask", retrievedSubtask.getName());
    }
}
