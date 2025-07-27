package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import http.exceptions.MethodNotAllowedException;
import models.managers.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(
            String basePath,
            TaskManager taskManager
            ) {
        super(basePath, taskManager);
    }

    @Override
    public void process(HttpExchange httpExchange) throws IOException {
        if (
                httpExchange.getRequestMethod().equals("GET")
                        && doesPathMatch(httpExchange, this.basePath + "$")
        ) {
            getHistory(httpExchange);
        } else {
            throw new MethodNotAllowedException();
        }
    }

    private void getHistory(HttpExchange httpExchange) throws IOException {
        sendOk(httpExchange, taskManager.getHistory());
    }
}