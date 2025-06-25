package models.managers.filebacked;

import models.managers.Managers;
import models.managers.inmemory.InMemoryTaskManagerTest;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @see FileBackedTaskManager
 */
public class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    private final Managers managers = new Managers();

    @BeforeEach
    public void initManager() {
        manager = managers.getfileBackedTaskManager();
    }

    @AfterEach
    public void clean() {
        manager.removeAllTasks(); // To delete generated files
    }

    /**
     * @see FileBackedTaskManager#getFullFileName()
     */
    @Test
    public void shouldGetFullFileName() {
        String fullFileName = "path" + File.separator + "file_name.ext";

        assertEquals(
                fullFileName,
                new FileBackedTaskManager(fullFileName).getFullFileName()
        );
    }

    /**
     * Check saving and loading tasks in file:
     * - "Заведите несколько разных задач, эпиков и подзадач."
     * - "Создайте новый FileBackedTaskManager-менеджер из этого же файла."
     * - "Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом."
     */
    @Test
    public void shouldSaveAndLoadTasks() {
        // Make tasks using a manager

        FileBackedTaskManager managerA = managers.getfileBackedTaskManager();

        Task task = managerA.createTask(new Task(0, "Task", ""));
        EpicTask epicTask = managerA.createTask(new EpicTask(0, "Epic", ""));
        SubTask subTask = managerA.createTask(new SubTask(0, epicTask.getId(), "Sub", ""));

        // Read saved tasks from the same file using another Manager

        FileBackedTaskManager managerB = managers.getfileBackedTaskManager(
                managerA.getFullFileName()
        );

        ArrayList<Task> tasks = managerB.getTasks();
        assertEquals(1, tasks.size());
        assertEqualsByContent(task, tasks.getFirst());

        ArrayList<EpicTask> epicTasks = managerB.getEpicTasks();
        assertEquals(1, epicTasks.size());
        assertEqualsByContent(epicTask, epicTasks.getFirst());

        ArrayList<SubTask> subTasks = managerB.getSubTasks();
        assertEquals(1, subTasks.size());
        assertEqualsByContent(subTask, subTasks.getFirst());

        // Remove the file
        managerA.removeAllTasks();
    }
}
