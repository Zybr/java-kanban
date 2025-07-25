package http.handlers;

import assertions.HttpResponseAssertions;
import assertions.TaskAssertions;
import http.tokens.TaskListTypeToken;
import models.factories.TasksFactory;
import models.tasks.Task;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public abstract class HandlerTest<T extends Task> extends HttpTest {
    abstract protected String getBasePath();

    abstract protected Class<T> getTaskClass();

    abstract protected ArrayList<T> getTasks();

    abstract protected T makeTask();

    protected T makeTask(int startHours, int endHours) {
        return TasksFactory.setTimeRange(
                makeTask(),
                startHours, endHours
        );
    }

    abstract protected T createTask();

    abstract protected T createTask(int startHours, int endHours);

    abstract protected ArrayList<T> createTasks(int number);

    public void testGetTasks() throws IOException, InterruptedException {
        ArrayList<T> tasks = createTasks(3);

        HttpResponse<String> response = doGetRequest(getBasePath());

        HttpResponseAssertions.assertOk(response);
        TaskAssertions.assertListsEqualByContent(
                tasks, // Manager Tasks
                serializer.fromJson( // Response Tasks
                        response.body(),
                        new TaskListTypeToken().getType()
                )
        );
    }

    public void testGetTask() throws IOException, InterruptedException {
        T task = createTask();

        HttpResponse<String> response = doGetRequest(getBasePath() + "/" + task.getId());

        HttpResponseAssertions.assertOk(response);
        TaskAssertions.assertEqualByContent(
                task, // Manager Task
                serializer.fromJson( // Response Tasks
                        response.body(),
                        getTaskClass()
                )
        );
    }

    public void testNotFoundTask() throws IOException, InterruptedException {
        T task = createTask();

        HttpResponse<String> response = doGetRequest(getBasePath() + "/" + (task.getId() + 1));

        HttpResponseAssertions.assertNotFound(response);
    }

    public void testDeleteTask() throws IOException, InterruptedException {
        int tasksCount = 3;
        ArrayList<T> tasks = createTasks(tasksCount);

        HttpResponse<String> response1 = doDeleteRequest(getBasePath() + "/" + tasks.get(1).getId());

        HttpResponseAssertions.assertOk(response1);
        Assertions.assertEquals(tasksCount - 1, getTasks().size());

        // Repeat the same request

        HttpResponse<String> response2 = doDeleteRequest(getBasePath() + "/" + tasks.get(1).getId());

        HttpResponseAssertions.assertOk(response2);
        Assertions.assertEquals(tasksCount - 1, getTasks().size());
    }

    public void testPostCreateTask() throws IOException, InterruptedException {
        T newTask = makeTask();

        HttpResponse<String> response = doPostRequest(getBasePath(), serializer.toJson(newTask));

        ArrayList<T> managerTasks = getTasks();
        HttpResponseAssertions.assertCreated(response);
        Assertions.assertEquals(1, managerTasks.size());
        TaskAssertions.assertEqualByContent(
                newTask,  // Passed Task
                managerTasks.getFirst() // Saved Task
        );
        TaskAssertions.assertEqualByContent(
                newTask, // Passed
                serializer.fromJson( // Returned Tasks
                        response.body(),
                        getTaskClass()
                )
        );
    }

    public void testRejectPostCreateIntersection() throws IOException, InterruptedException {
        createTask(100, 120);
        T newTask = makeTask(110, 130);

        HttpResponse<String> response = doPostRequest(getBasePath(), serializer.toJson(newTask));

        HttpResponseAssertions.assertNotAcceptable(response);
    }

    public void testPostUpdateTask() throws IOException, InterruptedException {
        T updatingTask = createTask();
        updatingTask.fill(makeTask());

        HttpResponse<String> response = doPostRequest(getBasePath(), serializer.toJson(updatingTask));

        ArrayList<T> managerTasks = getTasks();
        HttpResponseAssertions.assertCreated(response);
        Assertions.assertEquals(1, managerTasks.size());
        TaskAssertions.assertEqualByContent(
                updatingTask,  // Passed Task
                managerTasks.getFirst() // Saved Task
        );
        TaskAssertions.assertEqualByContent(
                updatingTask, // Passed
                serializer.fromJson( // Returned Tasks
                        response.body(),
                        getTaskClass()
                )
        );
    }

    public void testRejectPostUpdateIntersection() throws IOException, InterruptedException {
        createTask(100, 120);
        T updatingTask = createTask(130, 150);
        TasksFactory.setTimeRange(updatingTask, 110, 150);

        HttpResponse<String> response = doPostRequest(getBasePath(), serializer.toJson(updatingTask));

        HttpResponseAssertions.assertNotAcceptable(response);
    }
}
