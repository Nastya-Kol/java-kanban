package manager;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.google.gson.Gson;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private Gson gson;


    public HttpTaskServer(TaskManager taskManager, Gson gson) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.gson = gson;

        server.createContext("/tasks", new TaskHttpHandler(taskManager));
        server.createContext("/subtasks", new SubtaskHttpHandler(taskManager));
        server.createContext("/epics", new EpicHttpHandler(taskManager));
        server.createContext("/history", new HistoryHttpHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHttpHandler(taskManager));
    }

    public void start() {
        System.out.println("Сервер запущен на порту" + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefaultManager(), GsonBuilder.getGson());
        taskServer.start();
        taskServer.stop();
    }

}
