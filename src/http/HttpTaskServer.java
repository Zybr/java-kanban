package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.adapters.DurationAdapter;
import http.adapters.LocalDateTimeAdapter;
import http.handlers.*;
import models.managers.Managers;
import models.managers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static HttpTaskServer instance;
    private HttpServer server;
    private final TaskManager taskManger;
    private final Gson serializer;
    private final int port = 8081; // Local 8080 is occupied

    public static void main(String[] args) throws IOException {
        getInstance().start();
    }

    private static HttpTaskServer getInstance() {
        if (instance == null) {
            instance = new HttpTaskServer(new Managers().getfileBackedTaskManager());
        }

        return instance;
    }

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManger = taskManager;
        this.serializer = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public String getBaseUriStr() {
        return String.format("http://localhost:%d", port);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        mapHandlers();
        server.start();
    }

    public void stop() {
        server.stop(1);
    }

    public Gson getSerializer() {
        return this.serializer;
    }

    private void mapHandlers() {
        server.createContext("/tasks", new TasksHandler("/tasks", this.taskManger, this.serializer));
        server.createContext("/epics", new EpicsHandler("/epics", this.taskManger, this.serializer));
        server.createContext("/subtasks", new SubTasksHandler("/subtasks", this.taskManger, this.serializer));
        server.createContext("/history", new HistoryHandler("/history", this.taskManger, this.serializer));
        server.createContext("/prioritized", new PrioritizedHandler("/prioritized", this.taskManger, this.serializer));
    }
}
