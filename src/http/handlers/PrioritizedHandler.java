package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import http.exceptions.NotFoundException;
import models.managers.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(
            String basePath,
            TaskManager taskManager,
            Gson serializer
    ) {
        super(basePath, taskManager, serializer);
    }

    @Override
    public void process(HttpExchange httpExchange) throws IOException {
        if (
                httpExchange.getRequestMethod().equals("GET")
                        && doesPathMatch(httpExchange, this.basePath + "$")
        ) {
            getTasks(httpExchange);
        } else {
            throw new NotFoundException();
        }
    }

    private void getTasks(HttpExchange httpExchange) throws IOException {
        sendOk(httpExchange, taskManager.getPrioritizedTasks());
    }
}