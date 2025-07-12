package models.tasks;

import models.factories.TasksFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * @see Task
 */
class TaskTest {
    /**
     * "Проверьте, что экземпляры класса Task равны друг другу, если равен их id;"
     *
     * @see Task#equals(Object)
     */
    @Test
    public void shouldCompareTasksById() {
        Task taskA = TasksFactory.makeTask(1);
        Task taskB = TasksFactory.makeTask(1);
        Task taskC = TasksFactory.makeTask(2);

        Assertions.assertEquals(taskA, taskB);
        Assertions.assertNotEquals(taskA, taskC);
    }

    /**
     * "Проверьте, что экземпляры класса Task равны друг другу, если равен их id;"
     *
     * @see Task#equals(Object)
     */
    @Test
    public void shouldCompareInheritedTasksById() {
        Task taskA = TasksFactory.makeTask(1);
        Task epicTask = TasksFactory.makeEpic(1);
        Task subTask = TasksFactory.makeSub(epicTask.getId(), 1);
        Task taskB = TasksFactory.makeTask(2);

        Assertions.assertEquals(taskA, epicTask);
        Assertions.assertEquals(taskA, subTask);
        Assertions.assertNotEquals(taskA, taskB);
    }

    @Test
    public void shouldCalculateEndTime() {
        Task task = TasksFactory.makeTask(1);
        Assertions.assertEquals(
                task.getStartTime().plus(task.getDuration()),
                task.getEndTime()
        );
    }
}