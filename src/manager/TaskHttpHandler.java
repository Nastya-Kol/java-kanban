package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;

import exception.TimeConflictException;
import exception.ManagerSaveException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;

public class TaskHttpHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path);
                    break;
                case "POST":
                    handlePostRequest(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            String response = gson.toJson(taskManager.getTasks());
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            int id = extractId(path);
            Task task = taskManager.getTaskById(id);
            if (task != null) {
                sendSuccess(exchange, gson.toJson(task));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            try {
                if (task.getId() == 0) {
                    int newId = taskManager.saveTasks(task);
                    sendCreated(exchange, gson.toJson(taskManager.getTaskById(newId)));
                } else {
                    taskManager.updateTasks(task);
                    sendSuccess(exchange, gson.toJson(task));
                }
            } catch (TimeConflictException e) {
                sendNotAcceptable(exchange, e.getMessage());
            } catch (ManagerSaveException e) {
                sendInternalError(exchange, e.getMessage());
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            int id = extractId(path);
            taskManager.removeTasksById(id);
            sendSuccess(exchange, "Задача удалена");
        } else {
            sendNotFound(exchange);
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}