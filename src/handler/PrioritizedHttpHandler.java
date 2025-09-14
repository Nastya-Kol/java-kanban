package handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHttpHandler extends BaseHttpHandler {

    public PrioritizedHttpHandler(TaskManager taskManager) {
        super(taskManager);

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
