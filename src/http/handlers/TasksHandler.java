package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import http.exceptions.MethodNotAllowedException;
import http.exceptions.NotFoundException;
import http.serialization.SerializerFactory;
import models.managers.TaskManager;
import models.tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TasksHandler extends BaseHttpHandler {
    private final Gson serializer = SerializerFactory.getSerializer();

    public TasksHandler(
            String basePath,
            TaskManager taskManager
    ) {
        super(basePath, taskManager);
    }

    @Override
    public void process(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        boolean hasListPath = doesPathMatch(httpExchange, basePath + "$");
        boolean hasItemPath = doesPathMatch(httpExchange, basePath + "/\\d$");

        switch (method) {
            case "GET" -> {
                if (hasListPath) {
                    getTasks(httpExchange);
                } else if (hasItemPath) {
                    getTask(httpExchange);
                }
            }
            case "POST" -> {
                if (hasListPath) {
                    postTask(httpExchange);
                }
            }
            case "DELETE" -> {
                if (hasItemPath) {
                    deleteTask(httpExchange);
                }
            }
            default -> throw new MethodNotAllowedException();
        }
    }

    private void getTasks(HttpExchange httpExchange) throws IOException {
        ArrayList<Task> tasks = taskManager.getTasks();
        sendOk(httpExchange, tasks);
    }

    private void postTask(HttpExchange httpExchange) throws IOException {
        Task taskData = serializer.fromJson(
                new String(
                        httpExchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                ),
                Task.class
        );

        if (taskData.getId() == 0) {
            postCreateTask(httpExchange, taskData);
        } else {
            postUpdateTask(httpExchange, taskData);
        }
    }

    private void postCreateTask(HttpExchange httpExchange, Task taskData) throws IOException {
        sendCreated(
                httpExchange,
                taskManager.createTask(
                        taskData
                )
        );
    }

    private void postUpdateTask(HttpExchange httpExchange, Task taskData) throws IOException {
        taskManager.getTask(taskData.getId())
                .orElseThrow(NotFoundException::new);
        taskManager.updateTask(taskData);

        sendCreated(
                httpExchange,
                taskManager
                        .getTask(taskData.getId())
                        .orElseThrow()
        );
    }

    public void getTask(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );
        Task task = taskManager.getTask(id)
                .orElseThrow(NotFoundException::new);
        sendOk(httpExchange, task);
    }

    public void deleteTask(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );

        if (taskManager.getTask(id).isPresent()) {
            taskManager.removeTask(id);
        }

        sendOk(httpExchange, null);
    }
}
