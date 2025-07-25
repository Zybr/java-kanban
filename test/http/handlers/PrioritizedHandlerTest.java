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

public class PrioritizedHandlerTest extends HttpTest {
    @Test
    public void shouldGetPrioritized() throws IOException, InterruptedException {
        ArrayList<Task> prioritizedTasks = createPrioritized();

        HttpResponse<String> response = doGetRequest("/prioritized");

        HttpResponseAssertions.assertOk(response);
        TaskAssertions.assertListsEqualByContent(
                prioritizedTasks,
                serializer.fromJson(
                        response.body(),
                        new TaskListTypeToken().getType()
                )
        );
    }

    public ArrayList<Task> createPrioritized() {
        ArrayList<Task> prioritizedTasks = new ArrayList<>();

        // Create not prioritized Tasks
        manager.createTask(TasksFactory.setTimeRange(TasksFactory.makeTask(), 0, 0));
        manager.createTask(TasksFactory.setTimeRange(TasksFactory.makeTask(), 0, 0));
        // Create prioritized Tasks
        prioritizedTasks.add(manager.createTask(TasksFactory.makeTask()));
        prioritizedTasks.add(manager.createTask(TasksFactory.makeTask()));

        return prioritizedTasks;
    }
}
