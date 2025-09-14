package handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import exception.ManagerSaveException;

import java.io.IOException;
import java.util.List;

public class EpicHttpHandler extends BaseHttpHandler {

    public EpicHttpHandler(TaskManager taskManager) {
        super(taskManager);
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
        if (path.equals("/epics")) {
            String response = gson.toJson(taskManager.getEpics());
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            int id = extractId(path);
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                sendSuccess(exchange, gson.toJson(epic));
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            int id = extractEpicIdFromSubtasksPath(path);
            List<Subtask> subtasks = taskManager.getAllSubtaskByEpic(id);
            sendSuccess(exchange, gson.toJson(subtasks));
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            String body = readRequestBody(exchange);
            if (body == null || body.trim().isEmpty()) {

                sendBadRequest(exchange, "Тело запроса не может быть пустым");
                return;
            }
            Epic epic = gson.fromJson(body, Epic.class);

            try {
                if (epic.getId() == 0) {
                    int newId = taskManager.saveEpics(epic);
                    sendCreated(exchange, gson.toJson(taskManager.getEpicById(newId)));
                }
            } catch (ManagerSaveException e) {
                sendInternalError(exchange, e.getMessage());
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            int id = extractId(path);
            taskManager.removeEpicsById(id);
            sendSuccess(exchange, "Эпик удален");
        } else {
            sendNotFound(exchange);
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private int extractEpicIdFromSubtasksPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }
}