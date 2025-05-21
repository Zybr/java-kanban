package models.tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Task taskA = new Task(1, "Name", "");
        Task taskB = new Task(1, "Name", "");
        Task taskC = new Task(2, "Name", "");

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
        Task taskA = new Task(1, "Name", "");
        Task epicTask = new EpicTask(1, "Name", "");
        Task subTask = new SubTask(1, epicTask.getId(), "Name", "");
        Task taskB = new Task(2, "Name", "");

        Assertions.assertEquals(taskA, epicTask);
        Assertions.assertEquals(taskA, subTask);
        Assertions.assertNotEquals(taskA, taskB);
    }
}