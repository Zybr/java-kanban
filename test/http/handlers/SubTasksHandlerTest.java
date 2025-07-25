package http.handlers;

import models.factories.TasksFactory;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @see TasksHandler
 */
public class SubTasksHandlerTest extends HandlerTest<SubTask> {
    @Override
    protected String getBasePath() {
        return "subtasks";
    }

    @Override
    protected Class<SubTask> getTaskClass() {
        return SubTask.class;
    }

    @Test
    public void shouldGetTasks() throws IOException, InterruptedException {
        testGetTasks();
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
    protected ArrayList<SubTask> getTasks() {
        return manager.getSubTasks();
    }

    @Override
    protected SubTask makeTask() {
        EpicTask epic = manager.getEpicTasks().isEmpty()
                ? manager.createTask(TasksFactory.makeEpic())
                : manager.getEpicTasks().getFirst();
        return TasksFactory.makeSub(epic.getId());
    }

    @Override
    protected SubTask createTask() {
        return manager.createTask(makeTask());
    }

    @Override
    protected SubTask createTask(int startHours, int endHours) {
        return manager.createTask(makeTask(startHours, endHours));
    }

    @Override
    protected ArrayList<SubTask> createTasks(int number) {
        for (int i = 0; i < number; i++) {
            manager.createTask(makeTask());
        }

        return getTasks();
    }
}