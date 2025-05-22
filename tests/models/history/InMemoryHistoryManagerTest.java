package models.history;

import models.managers.Managers;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @see InMemoryHistoryManager
 */
class InMemoryHistoryManagerTest {
    /**
     * "Убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных."
     *
     * @see InMemoryHistoryManager#add(Task)
     * @see InMemoryHistoryManager#getHistory()
     */
    @Test
    public void shouldNotChangeHistoryByLink() {
        HistoryManager manager = Managers.getDefaultHistory();

        // Make tasks
        // Regular
        Task task = new Task(1, "Task", "Task Desc");
        Task originalTask = task.copy();
        manager.add(task);
        // Epic
        EpicTask epicTask = new EpicTask(2, "Epic", "Epic Desc");
        EpicTask originalEpicTask = epicTask.copy();
        manager.add(epicTask);
        // Sub
        SubTask subTask = new SubTask(3, epicTask.getId(), "Sub", "Sub Desc");
        SubTask originalSubTask = subTask.copy();
        manager.add(subTask);

        // Try to change the history tasks updating the original task
        task.setName(task.getName() + " [updated]");
        epicTask.setName(epicTask.getName() + " [updated]");
        subTask.setName(subTask.getName() + " [updated]");

        // Try to change the history tasks by link
        ArrayList<Task> history = manager.getHistory();
        history.getFirst().setName(history.getFirst() + " [updated]");

        // Check immutability
        history = manager.getHistory();
        assertEquals(originalTask.getName(), history.get(0).getName());
        assertEquals(originalEpicTask.getName(), history.get(1).getName());
        assertEquals(originalSubTask.getName(), history.get(2).getName());
    }
}