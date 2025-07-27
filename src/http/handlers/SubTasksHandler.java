package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.exceptions.MethodNotAllowedException;
import http.exceptions.NotFoundException;
import models.managers.TaskManager;
import models.tasks.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SubTasksHandler extends BaseHttpHandler {
    public SubTasksHandler(
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
        ArrayList<SubTask> tasks = taskManager.getSubTasks();
        sendOk(httpExchange, tasks);
    }

    private void postTask(HttpExchange httpExchange) throws IOException {
        SubTask taskData = serializer.fromJson(
                new String(
                        httpExchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                ),
                SubTask.class
        );

        if (taskData.getId() == 0) {
            postCreateTask(httpExchange, taskData);
        } else {
            postUpdateTask(httpExchange, taskData);
        }
    }

    private void postCreateTask(HttpExchange httpExchange, SubTask taskData) throws IOException {
        sendCreated(
                httpExchange,
                taskManager.createTask(
                        taskData
                )
        );
    }

    private void postUpdateTask(HttpExchange httpExchange, SubTask taskData) throws IOException {
        taskManager.getSubTask(taskData.getId())
                .orElseThrow(NotFoundException::new);
        taskManager.updateTask(taskData);

        sendCreated(
                httpExchange,
                taskManager
                        .getSubTask(taskData.getId())
                        .orElseThrow()
        );
    }

    public void getTask(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );
        SubTask task = taskManager.getSubTask(id)
                .orElseThrow(NotFoundException::new);
        sendOk(httpExchange, task);
    }

    public void deleteTask(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );

        if (taskManager.getSubTask(id).isPresent()) {
            taskManager.removeTask(id);
        }

        sendOk(httpExchange, null);
    }
}
