package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Subtask;
import exception.TimeConflictException;
import exception.ManagerSaveException;

import java.io.IOException;

public class SubtaskHttpHandler extends BaseHttpHandler {

    public SubtaskHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
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
        if (path.equals("/subtasks")) {
            String response = gson.toJson(taskManager.getSubtasks());
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            int id = extractId(path);
            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask != null) {
                sendSuccess(exchange, gson.toJson(subtask));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            String body = readRequestBody(exchange);
            if (body == null || body.trim().isEmpty()) {

                sendBadRequest(exchange, "Для создания подзадачи необходимо указать эпик");
                return;
            }
            Subtask subtask = gson.fromJson(body, Subtask.class);

            try {
                if (subtask.getId() == 0) {
                    int newId = taskManager.saveSubtasks(subtask);
                    sendCreated(exchange, gson.toJson(taskManager.getSubtaskById(newId)));
                } else {
                    taskManager.updateSubtasks(subtask);
                    sendSuccess(exchange, gson.toJson(subtask));
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
        if (path.matches("/subtasks/\\d+")) {
            int id = extractId(path);
            taskManager.removeSubtasksById(id);
            sendSuccess(exchange, "Подзадача удалена");
        } else {
            sendNotFound(exchange);
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
