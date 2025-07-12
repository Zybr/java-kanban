package models.tasks;

import models.factories.TasksFactory;
import models.managers.Managers;
import models.managers.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @see EpicTask
 */
class EpicTaskTest {
    private static Managers managers;
    private TaskManager manager;

    @BeforeAll
    protected static void initManagers() {
        managers = new Managers();
    }

    @BeforeEach
    public void initManger() {
        manager = managers.getDefault();
    }

    /**
     * "Проверьте, что объект Subtask нельзя сделать своим же эпиком;"
     * "Проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;" - Not possible using provided interfaces
     *
     * @see EpicTask#fill(Task)
     */
    @Test
    public void shouldNotSetSubAsEpic() {
        // Create Epic
        manager.createTask(TasksFactory.makeEpic());
        EpicTask epicTaskA = manager.getEpicTasks().getFirst();

        // Create Sub
        manager.createTask(TasksFactory.makeSub(epicTaskA.getId()));
        SubTask subTask = manager.getSubTasks().getFirst();
        Assertions.assertEquals(epicTaskA.getId(), subTask.getEpicId()); // The epic ID is correct

        // Try to change the epic ID
        manager.updateTask(new SubTask(
                subTask.getId(),
                subTask.getId(),
                subTask.getName(),
                subTask.getDescription(),
                subTask.getStartTime(),
                subTask.getDuration()
        ));
        subTask = manager.getSubTasks().getFirst();
        Assertions.assertEquals(epicTaskA.getId(), subTask.getEpicId()); // The epic ID is still the same
    }
}