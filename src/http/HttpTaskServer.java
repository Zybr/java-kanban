package http;

import com.sun.net.httpserver.HttpServer;
import http.handlers.*;
import models.managers.Managers;
import models.managers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static HttpTaskServer instance;
    private HttpServer server;
    private final TaskManager taskManger;
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

    private void mapHandlers() {
        server.createContext("/tasks", new TasksHandler("/tasks", this.taskManger));
        server.createContext("/epics", new EpicsHandler("/epics", this.taskManger));
        server.createContext("/subtasks", new SubTasksHandler("/subtasks", this.taskManger));
        server.createContext("/history", new HistoryHandler("/history", this.taskManger));
        server.createContext("/prioritized", new PrioritizedHandler("/prioritized", this.taskManger));
    }
}
