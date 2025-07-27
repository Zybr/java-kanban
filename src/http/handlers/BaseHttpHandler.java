package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.exceptions.MethodNotAllowedException;
import http.exceptions.NotFoundException;
import http.serialization.SerializerFactory;
import models.managers.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final String basePath;
    protected final TaskManager taskManager;
    protected final Gson serializer = SerializerFactory.getSerializer();

    public BaseHttpHandler(
            String basePath,
            TaskManager taskManager
    ) {
        this.basePath = basePath;
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            this.process(httpExchange);
        } catch (NotFoundException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendNotAcceptable(httpExchange, e.getMessage());
        } catch (MethodNotAllowedException e) {
            sendMethodNotAllowed(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(httpExchange, e.getMessage());
        }
    }

    protected abstract void process(HttpExchange httpExchange) throws IOException;

    protected void sendOk(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 200, body);
    }

    protected void sendCreated(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 201, body);
    }

    protected void sendNotFound(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 404, body != null ? body : "Data was not found");
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 405, body != null ? body : "Method not allowed");
    }

    protected void sendNotAcceptable(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 406, body != null ? body : "Can't be processed");
    }

    protected void sendServerError(HttpExchange exchange, Object body) throws IOException {
        send(exchange, 500, body != null ? body : "Internal server error");
    }

    protected void send(
            HttpExchange exchange,
            int statusCode,
            Object body
    ) throws IOException {
        String jsonText = body != null ? serializer.toJson(body) : "";
        byte[] bodyBytes = jsonText.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bodyBytes.length);
        exchange.getResponseBody().write(bodyBytes);
        exchange.close();
    }

    protected boolean doesPathMatch(HttpExchange httpExchange, String regExp) {
        String path = httpExchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(path);
        return matcher.find();
    }

    protected Optional<String> getPathPart(HttpExchange httpExchange, String regExp) {
        String path = httpExchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(path);

        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }
}