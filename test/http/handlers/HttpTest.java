package http.handlers;

import com.google.gson.Gson;
import http.HttpTaskServer;
import models.managers.TaskManager;
import models.managers.inmemory.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson serializer = server.getSerializer();
    HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    protected void init() throws IOException {
        manager.removeAllTasks();
        server.start();
    }

    @AfterEach
    protected void stopServer() {
        server.stop();
    }

    protected HttpResponse<String> doGetRequest(String path) throws IOException, InterruptedException {
        return client.send(
                HttpRequest
                        .newBuilder()
                        .uri(makeUri(path))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    protected HttpResponse<String> doPostRequest(String path, String jsonBody) throws IOException, InterruptedException {
        return client.send(
                HttpRequest
                        .newBuilder()
                        .uri(makeUri(path))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    protected HttpResponse<String> doDeleteRequest(String path) throws IOException, InterruptedException {
        return client.send(
                HttpRequest
                        .newBuilder()
                        .uri(makeUri(path))
                        .DELETE()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private URI makeUri(String path) {
        return URI.create(
                server.getBaseUriStr()
                        + "/"
                        + path.trim()
                        .replaceAll("^/", "")
                        .replaceAll("\\$", "")
        );
    }
}
