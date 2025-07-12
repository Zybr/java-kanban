package models.managers.filebacked;

import models.factories.TasksFactory;
import models.managers.AbstractTaskManagerTest;
import models.managers.Managers;
import models.managers.TaskManager;
import models.managers.filebacked.exceptions.ManagerLoadException;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @see FileBackedTaskManager
 */
public class FileBackedTaskManagerTest extends AbstractTaskManagerTest {
    private final Managers managers = new Managers();

    @Override
    protected TaskManager makeManager() {
        return managers.getfileBackedTaskManager();
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

        Task task = managerA.createTask(TasksFactory.makeTask());
        EpicTask epicTask = managerA.createTask(TasksFactory.makeEpic());
        SubTask subTask = managerA.createTask(TasksFactory.makeSub(epicTask.getId()));
        epicTask = managerA.getEpicTask(epicTask.getId()).orElseThrow(); // Refresh after adding Sub

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

    @Test
    public void shouldThrowExceptionForInvalidFileContent() throws IOException {
        String filePath = managers
                .getfileBackedTaskManager()
                .getFullFileName();
        Files.writeString(
                Path.of(filePath),
                "invalid;task;row"
        );
        assertThrows(
                ManagerLoadException.class,
                () -> managers.getfileBackedTaskManager(filePath)
        );
    }
}
