package models.history.tasks_list;

import models.tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * "Проверьте, что встроенный связный список версий, а также операции добавления и удаления работают корректно."
 *
 * @see TaskNodesList
 */
class TaskNodesListTest {
    /**
     * @see TaskNodesList#addTask(Task)
     */
    @Test
    public void shouldAddTask() {
        TaskNodesList list = new TaskNodesList();
        assertEquals(List.of(), getTaskIds(list.getTasks()));
        assertEquals(List.of(), getTaskIds(list.getReversedTask()));

        list.addTask(makeTask(0));
        assertEquals(List.of(0), getTaskIds(list.getTasks()));
        assertEquals(List.of(0), getTaskIds(list.getReversedTask()));

        list.addTask(makeTask(1));
        assertEquals(List.of(0, 1), getTaskIds(list.getTasks()));
        assertEquals(List.of(1, 0), getTaskIds(list.getReversedTask()));

        list.addTask(makeTask(2));
        assertEquals(List.of(0, 1, 2), getTaskIds(list.getTasks()));
        assertEquals(List.of(2, 1, 0), getTaskIds(list.getReversedTask()));
    }

    /**
     * @see TaskNodesList#removeNode(TaskNode)
     */
    @Test
    public void shouldRemoveNode() {
        TaskNodesList list = new TaskNodesList();
        assertEquals(List.of(), getTaskIds(list.getTasks()));
        assertEquals(List.of(), getTaskIds(list.getReversedTask()));
        List<TaskNode> nodes = new ArrayList<>();

        nodes.add(list.addTask(makeTask(0)));
        nodes.add(list.addTask(makeTask(1)));
        nodes.add(list.addTask(makeTask(2)));
        nodes.add(list.addTask(makeTask(3)));
        nodes.add(list.addTask(makeTask(4)));

        assertEquals(List.of(0, 1, 2, 3, 4), getTaskIds(list.getTasks()));
        assertEquals(List.of(4, 3, 2, 1, 0), getTaskIds(list.getReversedTask()));

        // Remove the last one
        list.removeNode(nodes.getLast());
        assertEquals(List.of(0, 1, 2, 3), getTaskIds(list.getTasks()));
        assertEquals(List.of(3, 2, 1, 0), getTaskIds(list.getReversedTask()));

        // Remove the first one
        list.removeNode(nodes.getFirst());
        assertEquals(List.of(1, 2, 3), getTaskIds(list.getTasks()));
        assertEquals(List.of(3, 2, 1), getTaskIds(list.getReversedTask()));

        // Remove the middle one
        list.removeNode(nodes.get(2));
        assertEquals(List.of(1, 3), getTaskIds(list.getTasks()));
        assertEquals(List.of(3, 1), getTaskIds(list.getReversedTask()));

        // Remove twice
        list.removeNode(nodes.get(1));
        assertEquals(List.of(3), getTaskIds(list.getTasks()));
        assertEquals(List.of(3), getTaskIds(list.getReversedTask()));
        list.removeNode(nodes.get(1));
        assertEquals(List.of(3), getTaskIds(list.getTasks()));
        assertEquals(List.of(3), getTaskIds(list.getReversedTask()));

        // Clear
        list.removeNode(nodes.get(3));
        assertEquals(List.of(), getTaskIds(list.getTasks()));
        assertEquals(List.of(), getTaskIds(list.getReversedTask()));
    }

    private Task makeTask(int id) {
        return new Task(id, Integer.toString(id), Integer.toString(id));
    }

    private List<Integer> getTaskIds(List<Task> tasks) {
        return tasks.stream().map(Task::getId).toList();
    }
}
