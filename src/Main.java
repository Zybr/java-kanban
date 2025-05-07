import models.TaskManager;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import models.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.Arrays;

import static tests.Assertions.*;

public class Main {
    private static TaskManager taskManager;

    public static void main(String[] args) {
        printHead("Empty Manager");

        taskManager = new TaskManager();
        assertSizes(0, 0, 0, "Task lists are empty at the beginning");

        printHead("Regular Tasks");

        // Create
        taskManager.createTask(new Task(0, "Task A", "Task A desc"));
        taskManager.createTask(new Task(0, "Task B", "Task B desc"));
        // Check creation
        assertSizes(2, 0, 0, "2 regular Tasks should be added");
        ArrayList<Task> tasks = taskManager.getTasks();
        assertTaskContent(
                tasks.get(0),
                1,
                "Task A",
                "Task A desc",
                TaskStatus.NEW,
                "Task A should be created"
        );
        assertTaskContent(
                tasks.get(1),
                2,
                "Task B",
                "Task B desc",
                TaskStatus.NEW,
                "Task B should be created"
        );
        // Update
        Task taskUpdate = new Task(1, "Task A [updated]", "Task A desc [updated]");
        taskUpdate.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(taskUpdate);
        // Check updating
        assertTaskContent(
                taskManager.getTask(1),
                1,
                "Task A [updated]",
                "Task A desc [updated]",
                TaskStatus.IN_PROGRESS,
                "Task A should be updated"
        );

        printHead("Epic Tasks");

        // Create
        taskManager.createTask(new EpicTask(0, "Epic A", "Epic A desc"));
        taskManager.createTask(new EpicTask(0, "Epic B", "Epic B desc"));
        // Check creation
        assertSizes(2, 2, 0, "2 Epics should be created");
        ArrayList<EpicTask> epics = taskManager.getEpicTasks();
        // The tasks should contain passed attributes with incrementing ID
        assertTaskContent(
                epics.get(0),
                3,
                "Epic A",
                "Epic A desc",
                TaskStatus.NEW,
                "Epic A should be created"
        );
        assertTaskContent(
                epics.get(1),
                4,
                "Epic B",
                "Epic B desc",
                TaskStatus.NEW,
                "Epic B should be created"
        );
        // Update
        EpicTask epicUpdate = new EpicTask(3, "Epic A [updated]", "Epic A desc [updated]");
        epicUpdate.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(epicUpdate);
        // Check updating
        assertTaskContent(
                taskManager.getEpicTask(3),
                3,
                "Epic A [updated]",
                "Epic A desc [updated]",
                TaskStatus.NEW, // (!) The status is still NEW because user can't change it directly
                "Epic A should be updated"
        );

        printHead("Sub Tasks");

        // Create
        taskManager.createTask(new SubTask(0, epics.get(0).getId(), "Sub A.1", "Sub A.1 desc"));
        taskManager.createTask(new SubTask(0, epics.get(0).getId(), "Sub A.2", "Sub A.2 desc"));
        taskManager.createTask(new SubTask(0, epics.get(1).getId(), "Sub B.1", "Sub B.1 desc"));
        // Check creation
        assertSizes(2, 2, 3, "3 Subs should be added");
        ArrayList<SubTask> subs = taskManager.getSubTasks();
        // They should contain passed attributes with incrementing ID
        assertTaskContent(
                subs.get(0),
                5,
                "Sub A.1",
                "Sub A.1 desc",
                TaskStatus.NEW,
                "Sub A.1 should be created"
        );
        assertTaskContent(
                subs.get(1),
                6,
                "Sub A.2",
                "Sub A.2 desc",
                TaskStatus.NEW,
                "Sub A.2 should be created"
        );
        assertTaskContent(
                subs.get(2),
                7,
                "Sub B.1",
                "Sub B.1 desc",
                TaskStatus.NEW,
                "Sub B.1 should be created"
        );
        // Check Epics->Subs relations
        assertTrue(Arrays.equals( // 3->{5,6} "Epic A"(ID 3) should include "Sub A.1"(ID 5) and "Sub A.2"(ID 6)
                new Object[]{5, 6},
                taskManager.getSubTasksOfEpic(epics.get(0).getId()).stream().map(Task::getId).toArray()
        ));
        assertTrue(Arrays.equals( // 4->{7} "Epic B"(ID 4) should include "Sub B.1"(ID 7)
                new Object[]{7},
                taskManager.getSubTasksOfEpic(epics.get(1).getId()).stream().map(Task::getId).toArray()
        ));

        printHead("Epic Tasks statuses");

        assertTaskStatus(3, TaskStatus.NEW);
        updateStatus(5, TaskStatus.IN_PROGRESS); // Sub New + Sub In-progress = Epic In-Progress
        assertTaskStatus(3, TaskStatus.IN_PROGRESS);
        updateStatus(5, TaskStatus.DONE);
        assertTaskStatus(3, TaskStatus.IN_PROGRESS); // Sub New + Sub Done = Epic In-Progress
        updateStatus(6, TaskStatus.DONE);
        assertTaskStatus(3, TaskStatus.DONE); // Sub Done + Sub Done = Epic Done

        printHead("Removing");

        taskManager.removeTask(5);
        assertNull(taskManager.getSubTask(5), "Sub(ID 5) should be removed");
        assertSizes(2, 2, 2, "1 Sub should be removed");
        assertTaskStatus(3, TaskStatus.DONE); // Sub Done = Epic Done
        taskManager.removeTask(6);
        assertNull(taskManager.getSubTask(6), "Sub(ID 6) should be remove");
        assertSizes(2, 2, 1, "1 Sub should be removed");
        assertTaskStatus(3, TaskStatus.NEW); // No Tasks = Epic New
        taskManager.removeTasks();
        assertSizes(0, 2, 1, "Regular tasks should be removed");

        printHead("Cascading Removing");

        taskManager.removeTask(4);
        assertSizes(0, 1, 0, "1 Sub and 1 Epic should be removed");
        assertNull(taskManager.getEpicTask(4), "Sub (ID 4) should be removed");
        assertNull(taskManager.getSubTask(7), "Epic (ID 7) should be removed");

        // Result message
        System.out.println("\n--------------------------------------------------");
        System.out.println("\nTests passed");
    }

    // Helpers >>>

    private static void printHead(String text) {
        System.out.println("\n[ " + text + " ]\n");
    }

    private static void updateStatus(int id, TaskStatus newStatus) {
        Task task = getTask(id);
        task.setStatus(newStatus);

        switch (task) {
            case EpicTask epicTask -> {
                taskManager.updateTask((EpicTask) task);
            }
            case SubTask subTask -> {
                taskManager.updateTask((SubTask) task);
            }
            case Task regularTask -> {
                taskManager.updateTask(task);
            }
        }
    }

    private static Task getTask(int id) {
        Task task = taskManager.getTask(id);

        if (task == null) {
            task = taskManager.getEpicTask(id);
        }

        if (task == null) {
            task = taskManager.getSubTask(id);
        }

        return task;
    }

    // <<< Helpers

    // Asserts >>>

    // Asserts. Check current number of tasks for ech type.

    private static void assertSizes(int tasksSize, int epicsSize, int subsSize, String message) {
        String prefix = (message != null) ? message + "; " : "";
        assertEquals(tasksSize, taskManager.getTasks().size(), prefix + "Number of regular Tasks");
        assertEquals(epicsSize, taskManager.getEpicTasks().size(), prefix + "Number of Epics");
        assertEquals(subsSize, taskManager.getSubTasks().size(), prefix + "Number of Subs");
    }

    // Asserts. Check attributes values of Task.

    private static void assertTaskContent(Task task, int id, String name, String desc, TaskStatus status) {
        assertTaskContent(task, id, name, desc, status, null);
    }

    private static void assertTaskContent(Task task, int id, String name, String desc, TaskStatus status, String message) {
        String prefix = (message != null) ? message + "; " : "";
        assertEquals(id, task.getId(), prefix + "Task ID");
        assertEquals(name, task.getName(), prefix + "Task name");
        assertEquals(desc, task.getDescription(), prefix + "Task description");
        assertEquals(status, task.getStatus(), prefix + "Task status");
    }

    // Asserts. Check current Task status.

    private static void assertTaskStatus(int id, TaskStatus expectedStatus) {
        assertEquals(expectedStatus, (getTask(id)).getStatus(), "Task Status");
    }

    // <<< Asserts
}
