package http.handlers;

import assertions.HttpResponseAssertions;
import assertions.TaskAssertions;
import http.tokens.SubTaskListTypeToken;
import models.factories.TasksFactory;
import models.tasks.EpicTask;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;

/**
 * @see TasksHandler
 */
public class EpicsHandlerTest extends HandlerTest<EpicTask> {
    @Override
    protected String getBasePath() {
        return "epics";
    }

    @Override
    protected Class<EpicTask> getTaskClass() {
        return EpicTask.class;
    }

    @Test
    public void shouldGetTasks() throws IOException, InterruptedException {
        testGetTasks();
    }

    @Test
    public void shouldGetSubTask() throws IOException, InterruptedException {
        EpicTask epic = manager.createTask(TasksFactory.makeEpic());
        manager.createTask(TasksFactory.makeSub(epic.getId()));
        manager.createTask(TasksFactory.makeSub(epic.getId()));

        HttpResponse<String> response = doGetRequest(String.format("epics/%d/subtasks", epic.getId()));

        HttpResponseAssertions.assertOk(response);
        TaskAssertions.assertListsEqualByContent(
                manager.getEpicSubTasks(epic.getId()), // Manager Tasks
                serializer.fromJson( // Response Tasks
                        response.body(),
                        new SubTaskListTypeToken().getType()
                )
        );
    }

    @Test
    public void shouldGetSubTaskNotFound() throws IOException, InterruptedException {
        EpicTask epic = manager.createTask(TasksFactory.makeEpic());

        HttpResponse<String> response = doGetRequest(String.format("epics/%d/subtasks", epic.getId() + 1));

        HttpResponseAssertions.assertNotFound(response);
    }

    @Test
    public void shouldGetTask() throws IOException, InterruptedException {
        testGetTask();
    }

    @Test
    public void shouldNotFoundTask() throws IOException, InterruptedException {
        testNotFoundTask();
    }

    @Test
    public void shouldDeleteTask() throws IOException, InterruptedException {
        testDeleteTask();
    }

    @Test
    public void shouldPostTask() throws IOException, InterruptedException {
        testPostCreateTask();
    }

    @Override
    protected ArrayList<EpicTask> getTasks() {
        return manager.getEpicTasks();
    }


    @Override
    protected EpicTask makeTask() {
        return TasksFactory.makeEpic();
    }

    @Override
    protected EpicTask createTask() {
        return manager.createTask(
                makeTask()
        );
    }

    @Override
    protected EpicTask createTask(int startHours, int endHours) {
        return manager.createTask(
                makeTask(startHours, endHours)
        );
    }

    @Override
    protected ArrayList<EpicTask> createTasks(int number) {
        for (int i = 0; i < number; i++) {
            manager.createTask(makeTask());
        }

        return getTasks();
    }
}