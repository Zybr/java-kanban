package models.history;

import models.factories.TasksFactory;
import models.managers.Managers;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @see InMemoryHistoryManager
 */
class InMemoryHistoryManagerTest {
    /**
     * @see InMemoryHistoryManager#add(Task)
     * @see InMemoryHistoryManager#getHistory()
     */
    @Test
    public void shouldAddTask() {
        InMemoryHistoryManager manager = (InMemoryHistoryManager) Managers.getDefaultHistory();
        List<Task> tasks = fillHistory(manager);

        assertEquals(
                tasks,
                manager.getHistory()
        );
    }

    /**
     * @see InMemoryHistoryManager#add(Task)
     * @see InMemoryHistoryManager#remove(int)
     * @see InMemoryHistoryManager#getHistory()
     */
    @Test
    public void shouldRemoveTask() {
        InMemoryHistoryManager manager = (InMemoryHistoryManager) Managers.getDefaultHistory();
        assertEquals(
                List.of(),
                manager.getHistory()
        );

        List<Task> tasks = fillHistory(manager);
        manager.remove(tasks.get(1).getId());

        assertEquals(
                List.of(0, 2),
                getTaskIds(manager.getHistory())
        );
    }

    /**
     * - "Убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных."
     * - "С помощью сеттеров экземпляры задач позволяют изменить любое своё поле,
     * но это может повлиять на данные внутри менеджера."
     * - "Протестируйте эти кейсы и подумайте над возможными вариантами решения проблемы."
     *
     * @see InMemoryHistoryManager#add(Task)
     * @see InMemoryHistoryManager#getHistory()
     */
    @Test
    public void shouldNotChangeHistoryByLink() {
        InMemoryHistoryManager manager = (InMemoryHistoryManager) Managers.getDefaultHistory();

        // Make tasks
        // Regular
        Task task = TasksFactory.makeTask(1);
        Task originalTask = task.copy();
        manager.add(task);
        // Epic
        EpicTask epicTask = TasksFactory.makeEpic(10);
        EpicTask originalEpicTask = epicTask.copy();
        manager.add(epicTask);
        // Sub
        SubTask subTask = TasksFactory.makeSub(epicTask.getId());
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

    /**
     * Check that added tasks are unique and sorted by time of adding.
     *
     * @see InMemoryHistoryManager#add(Task)
     * @see InMemoryHistoryManager#getHistory()
     */
    @Test
    public void shouldReplaceOldTasks() {
        InMemoryHistoryManager manager = (InMemoryHistoryManager) Managers.getDefaultHistory();
        List<Task> tasks = fillHistory(manager);
        assertEquals(
                List.of(0, 1, 2),
                getTaskIds(manager.getHistory())
        );

        manager.add(tasks.get(0));
        assertEquals(
                List.of(1, 2, 0),
                getTaskIds(manager.getHistory())
        );

        manager.add(tasks.get(2));
        assertEquals(
                List.of(1, 0, 2),
                getTaskIds(manager.getHistory())
        );

        manager.add(tasks.get(1));
        assertEquals(
                List.of(0, 2, 1),
                getTaskIds(manager.getHistory())
        );
    }

    private List<Task> fillHistory(InMemoryHistoryManager manager) {
        List<Task> tasks = new ArrayList<>();

        for (int id = 0; id <= 2; id++) {
            Task task = TasksFactory.makeTask(id);
            manager.add(task);
            tasks.add(task);
        }

        return tasks;
    }

    private List<Integer> getTaskIds(List<Task> tasks) {
        return tasks.stream().map(Task::getId).toList();
    }
}