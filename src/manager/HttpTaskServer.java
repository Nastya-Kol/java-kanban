package manager;

import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import handler.*;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer(TaskManager taskManager, Gson gson) throws IOException {

        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TaskHttpHandler(taskManager));
        server.createContext("/subtasks", new SubtaskHttpHandler(taskManager));
        server.createContext("/epics", new EpicHttpHandler(taskManager));
        server.createContext("/history", new HistoryHttpHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHttpHandler(taskManager));
    }

    public void start() {
        System.out.println("Сервер запущен на порту: " + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefaultManager(), getGson());
        taskServer.start();

    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }
}
