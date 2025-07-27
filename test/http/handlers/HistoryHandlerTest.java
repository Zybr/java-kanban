package http.handlers;

import assertions.HttpResponseAssertions;
import assertions.TaskAssertions;
import http.tokens.TaskListTypeToken;
import models.factories.TasksFactory;
import models.tasks.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class HistoryHandlerTest extends HttpTest {
    @Test
    public void shouldGetHistory() throws IOException, InterruptedException {
        ArrayList<Task> history = createHistory();

        HttpResponse<String> response = doGetRequest("/history");

        HttpResponseAssertions.assertOk(response);
        TaskAssertions.assertListsEqualByContent(
                history,
                serializer.fromJson(
                        response.body(),
                        new TaskListTypeToken().getType()
                )
        );
    }

    public ArrayList<Task> createHistory() {
        Task createdTask = manager.createTask(TasksFactory.makeTask());
        ArrayList<Task> history = new ArrayList<>();
        history.add(manager.getTask(createdTask.getId()).orElseThrow());

        return history;
    }
}
