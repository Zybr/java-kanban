package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import http.exceptions.NotFoundException;
import models.managers.TaskManager;
import models.tasks.EpicTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(
            String basePath,
            TaskManager taskManager,
            Gson serializer
    ) {
        super(basePath, taskManager, serializer);
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
                } else if (doesPathMatch(httpExchange, basePath + "/\\d/subtasks$")) {
                    getSubTasks(httpExchange);
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
            default -> throw new NotFoundException();
        }
    }

    private void getTasks(HttpExchange httpExchange) throws IOException {
        ArrayList<EpicTask> epics = taskManager.getEpicTasks();
        sendOk(httpExchange, epics);
    }

    private void getSubTasks(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );
        EpicTask task = taskManager.getEpicTask(id)
                .orElseThrow(NotFoundException::new);
        sendOk(httpExchange, taskManager.getEpicSubTasks(task.getId()));
    }

    private void postTask(HttpExchange httpExchange) throws IOException {
        sendCreated(
                httpExchange,
                taskManager.createTask(
                        serializer.fromJson(
                                new String(
                                        httpExchange.getRequestBody().readAllBytes(),
                                        StandardCharsets.UTF_8
                                ),
                                EpicTask.class
                        )
                )
        );
    }

    public void getTask(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );
        EpicTask task = taskManager.getEpicTask(id)
                .orElseThrow(NotFoundException::new);
        sendOk(httpExchange, task);
    }

    public void deleteTask(HttpExchange httpExchange) throws IOException {
        int id = Integer.parseInt(
                getPathPart(httpExchange, basePath + "/(\\d+)")
                        .orElseThrow(NotFoundException::new)
        );

        if (taskManager.getEpicTask(id).isPresent()) {
            taskManager.removeTask(id);
        }

        sendOk(httpExchange, null);
    }
}
