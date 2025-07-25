package http.handlers;

import models.factories.TasksFactory;
import models.tasks.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @see TasksHandler
 */
public class TasksHandlerTest extends HandlerTest<Task> {
    @Override
    protected String getBasePath() {
        return "tasks";
    }

    @Override
    protected Class<Task> getTaskClass() {
        return Task.class;
    }

    @Test
    public void shouldGetTasks() throws IOException, InterruptedException {
        testGetTask();
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
    public void shouldPostCreateTask() throws IOException, InterruptedException {
        testPostCreateTask();
    }

    @Test
    public void shouldRejectPostCreateIntersection() throws IOException, InterruptedException {
        testRejectPostCreateIntersection();
    }

    @Test
    public void shouldPostUpdateTask() throws IOException, InterruptedException {
        testPostUpdateTask();
    }

    @Test
    public void shouldRejectPostUpdateIntersection() throws IOException, InterruptedException {
        testRejectPostUpdateIntersection();
    }

    @Override
    protected ArrayList<Task> getTasks() {
        return manager.getTasks();
    }


    @Override
    protected Task makeTask() {
        return TasksFactory.makeTask();
    }

    @Override
    protected Task createTask() {
        return manager.createTask(
                makeTask()
        );
    }

    @Override
    protected Task createTask(int startHours, int endHours) {
        return manager.createTask(
                makeTask(startHours, endHours)
        );
    }

    @Override
    protected ArrayList<Task> createTasks(int number) {
        for (int i = 0; i < number; i++) {
            manager.createTask(makeTask());
        }

        return getTasks();
    }
}