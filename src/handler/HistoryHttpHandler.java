package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHttpHandler extends BaseHttpHandler {

    public HistoryHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod()) && "/history".equals(exchange.getRequestURI().getPath())) {
            List<Task> history = taskManager.getHistory();
            sendSuccess(exchange, gson.toJson(history));
        } else {
            sendNotFound(exchange);
        }
    }
}
