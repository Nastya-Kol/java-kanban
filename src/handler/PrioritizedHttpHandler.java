package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHttpHandler extends BaseHttpHandler {

    public PrioritizedHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod()) && "/prioritized".equals(exchange.getRequestURI().getPath())) {
            List<Task> prioritized = taskManager.getPrioritizedTasks();
            sendSuccess(exchange, gson.toJson(prioritized));
        } else {
            sendNotFound(exchange);
        }
    }
}
