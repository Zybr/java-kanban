package models.managers;

import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import models.tasks.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @see InMemoryTaskManager
 */
class InMemoryTaskManagerTest {
    private InMemoryTaskManager manager;

    @BeforeEach
    public void initManager() {
        manager = (InMemoryTaskManager) new Managers().getDefault();
    }

    /**
     * @see InMemoryTaskManager#createTask(Task)
     * @see InMemoryTaskManager#updateTask(Task)
     * @see InMemoryTaskManager#removeTask(int)
     */
    @Test
    public void shouldManageRegularTasks() {
        // Creation
        manager.createTask(new Task(0, "Task A", ""));
        manager.createTask(new Task(0, "Task B", ""));
        ArrayList<Task> tasks = manager.getTasks();
        assertEquals(2, tasks.size());
        assertTaskContent(tasks.get(0), 1, "Task A");
        assertTaskContent(tasks.get(1), 2, "Task B");

        // Updating
        Task task = tasks.get(0);
        task.setName(task.getName() + " [updated]");
        manager.updateTask(task);
        assertTaskContent(tasks.getFirst(), 1, task.getName());

        // Removing
        manager.removeTask(task.getId());
        assertEquals(1, manager.getTasks().size());
    }

    /**
     * @see InMemoryTaskManager#createTask(EpicTask)
     * @see InMemoryTaskManager#updateTask(SubTask)
     * @see InMemoryTaskManager#removeTask(int)
     */
    @Test
    public void shouldEpicStatusDependOnSubs() {
        EpicTask epic = manager.createTask(new EpicTask(0, "Epic", ""));
        assertEquals(TaskStatus.NEW, epic.getStatus());

        SubTask subA = manager.createTask(new SubTask(0, epic.getId(), "Sub A", ""));
        SubTask subB = manager.createTask(new SubTask(0, epic.getId(), "Sub B", ""));
        assertEquals(TaskStatus.NEW, manager.getEpicTask(epic.getId()).getStatus());

        // Sub In Progress + Sub Done = Epic In Progress
        subA.setStatus(TaskStatus.DONE);
        manager.updateTask(subA);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTask(epic.getId()).getStatus());

        // Sub Done + Sub Done = Epic Done
        subB.setStatus(TaskStatus.DONE);
        manager.updateTask(subB);
        assertEquals(TaskStatus.DONE, manager.getEpicTask(epic.getId()).getStatus());

        manager.removeSubTasks();
        assertEquals(TaskStatus.NEW, manager.getEpicTask(epic.getId()).getStatus());
    }

    /**
     * @see InMemoryTaskManager#removeTask(int)
     */
    @Test
    public void shouldRemoveCascade() {
        EpicTask epic = manager.createTask(new EpicTask(0, "Epic", ""));
        manager.createTask(new SubTask(0, epic.getId(), "Sub A", ""));
        manager.createTask(new SubTask(0, epic.getId(), "Sub B", ""));
        assertEquals(1, manager.getEpicTasks().size());
        assertEquals(2, manager.getSubTasks().size());

        manager.removeTask(epic.getId());
        assertEquals(0, manager.getEpicTasks().size());
        assertEquals(0, manager.getSubTasks().size());
    }

    /**
     * "Проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;"
     *
     * @see InMemoryTaskManager#createTask
     * @see InMemoryTaskManager#getTask(int)
     * @see InMemoryTaskManager#getEpicTask(int)
     * @see InMemoryTaskManager#getSubTask(int)
     */
    @Test
    public void shouldAddTasks() {
        // Regular Tasks
        manager.createTask(new Task(0, "Task", ""));
        Task regularTask = manager.getTasks().getFirst();
        assertEquals(
                regularTask,
                manager.getTask(regularTask.getId())
        );

        // Epic Tasks
        manager.createTask(new EpicTask(0, "Epic", ""));
        EpicTask epicTask = manager.getEpicTasks().getFirst();
        assertEquals(
                epicTask,
                manager.getEpicTask(epicTask.getId())
        );

        // Sub Tasks
        manager.createTask(new SubTask(0, epicTask.getId(), "Sub", ""));
        SubTask subTask = manager.getSubTasks().getFirst();
        assertEquals(
                subTask,
                manager.getSubTask(subTask.getId())
        );
    }

    /**
     * "Проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;"
     *
     * @see InMemoryTaskManager#createTask
     */
    @Test
    public void shouldNotConflictByIds() {
        manager.createTask(new Task(10, "Task A", ""));
        manager.createTask(new Task(20, "Task B", ""));
        ArrayList<Task> regularTasks = manager.getTasks();
        assertEquals(2, regularTasks.size());
        assertTaskContent(regularTasks.get(0), 1, "Task A");
        assertTaskContent(regularTasks.get(1), 2, "Task B");

        manager.createTask(new EpicTask(10, "Epic A", ""));
        manager.createTask(new EpicTask(20, "Epic B", ""));
        ArrayList<EpicTask> epicTasks = manager.getEpicTasks();
        assertEquals(2, epicTasks.size());
        assertTaskContent(epicTasks.get(0), 3, "Epic A");
        assertTaskContent(epicTasks.get(1), 4, "Epic B");

        manager.createTask(new SubTask(10, epicTasks.get(0).getId(), "Sub A", ""));
        manager.createTask(new SubTask(20, epicTasks.get(1).getId(), "Sub B", ""));
        ArrayList<SubTask> subTasks = manager.getSubTasks();
        assertEquals(2, subTasks.size());
        assertTaskContent(subTasks.get(0), 5, "Sub A");
        assertTaskContent(subTasks.get(1), 6, "Sub B");
    }

    /**
     * "Создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер"
     */

    // Changing by link >>>
    @Test
    public void shouldNotTaskBeChangeableByLink() {
        Task sourceTask = new Task(0, "Name", "Description");
        manager.createTask(sourceTask);

        Task originalTask = sourceTask.copy();
        sourceTask.setName(sourceTask.getName() + " [updated]");
        sourceTask.setStatus(TaskStatus.DONE);
        sourceTask.setDescription(sourceTask.getDescription() + " [updated]");

        // Tyr to change the history by link
        ArrayList<Task> tasks = manager.getTasks();
        tasks.getFirst().setName(tasks.getFirst() + " [updated]");

        assertEqualsByContent(
                originalTask,
                manager.getTasks().getFirst()
        );
    }

    @Test
    public void shouldNotEpicBeChangeableByLink() {
        EpicTask sourceTask = new EpicTask(0, "Name", "Description");
        manager.createTask(sourceTask);

        EpicTask originalTask = sourceTask.copy();
        sourceTask.setName(sourceTask.getName() + " [updated]");
        sourceTask.setStatus(TaskStatus.DONE);
        sourceTask.setDescription(sourceTask.getDescription() + " [updated]");

        assertEqualsByContent(
                originalTask,
                manager.getEpicTasks().getFirst()
        );
    }

    @Test
    public void shouldNotSubBeChangeableByLink() {
        manager.createTask(new EpicTask(0, "Epic", "Epic description"));
        EpicTask epicTask = manager.getEpicTasks().getFirst();

        SubTask sourceTask = new SubTask(0, epicTask.getId(), "Name", "Description");
        manager.createTask(sourceTask);
        SubTask originalTask = sourceTask.copy();

        sourceTask.setName(sourceTask.getName() + " [updated]");
        sourceTask.setStatus(TaskStatus.DONE);
        sourceTask.setDescription(sourceTask.getDescription() + " [updated]");

        assertEqualsByContent(
                originalTask,
                manager.getSubTasks().getFirst()
        );
    }

    // <<< Changing by link

    private void assertTaskContent(Task task, int id, String name) {
        assertEquals(id, task.getId());
        assertEquals(name, task.getName());
    }

    private void assertEqualsByContent(Task taskA, Task taskB) {
        assertEquals(taskA.getName(), taskB.getName());
        assertEquals(taskA.getStatus(), taskB.getStatus());
        assertEquals(taskA.getDescription(), taskB.getDescription());
    }
}