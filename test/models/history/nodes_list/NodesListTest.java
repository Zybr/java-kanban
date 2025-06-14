package models.history.nodes_list;

import models.tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * "Проверьте, что встроенный связный список версий, а также операции добавления и удаления работают корректно."
 *
 * @see NodesList
 */
class NodesListTest {
    /**
     * @see NodesList#push(Node)
     */
    @Test
    public void shouldPushTask() {
        NodesList list = new NodesList();
        assertEquals(List.of(), getTaskIds(list.getValues()));
        assertEquals(List.of(), getTaskIds(list.getReversedValues()));

        list.push(makeNode(0));
        assertEquals(List.of(0), getTaskIds(list.getValues()));
        assertEquals(List.of(0), getTaskIds(list.getReversedValues()));

        list.push(makeNode(1));
        assertEquals(List.of(0, 1), getTaskIds(list.getValues()));
        assertEquals(List.of(1, 0), getTaskIds(list.getReversedValues()));

        list.push(makeNode(2));
        assertEquals(List.of(0, 1, 2), getTaskIds(list.getValues()));
        assertEquals(List.of(2, 1, 0), getTaskIds(list.getReversedValues()));
    }

    /**
     * @see NodesList#remove(Node)
     */
    @Test
    public void shouldRemoveNode() {
        NodesList list = new NodesList();
        assertEquals(List.of(), getTaskIds(list.getValues()));
        assertEquals(List.of(), getTaskIds(list.getReversedValues()));
        List<Node> nodes = new ArrayList<>();

        nodes.add(list.push(makeNode(0)));
        nodes.add(list.push(makeNode(1)));
        nodes.add(list.push(makeNode(2)));
        nodes.add(list.push(makeNode(3)));
        nodes.add(list.push(makeNode(4)));

        assertEquals(List.of(0, 1, 2, 3, 4), getTaskIds(list.getValues()));
        assertEquals(List.of(4, 3, 2, 1, 0), getTaskIds(list.getReversedValues()));

        // Remove the last one
        list.remove(nodes.getLast());
        assertEquals(List.of(0, 1, 2, 3), getTaskIds(list.getValues()));
        assertEquals(List.of(3, 2, 1, 0), getTaskIds(list.getReversedValues()));

        // Remove the first one
        list.remove(nodes.getFirst());
        assertEquals(List.of(1, 2, 3), getTaskIds(list.getValues()));
        assertEquals(List.of(3, 2, 1), getTaskIds(list.getReversedValues()));

        // Remove the middle one
        list.remove(nodes.get(2));
        assertEquals(List.of(1, 3), getTaskIds(list.getValues()));
        assertEquals(List.of(3, 1), getTaskIds(list.getReversedValues()));

        // Remove twice
        list.remove(nodes.get(1));
        assertEquals(List.of(3), getTaskIds(list.getValues()));
        assertEquals(List.of(3), getTaskIds(list.getReversedValues()));
        list.remove(nodes.get(1));
        assertEquals(List.of(3), getTaskIds(list.getValues()));
        assertEquals(List.of(3), getTaskIds(list.getReversedValues()));

        // Clear
        list.remove(nodes.get(3));
        assertEquals(List.of(), getTaskIds(list.getValues()));
        assertEquals(List.of(), getTaskIds(list.getReversedValues()));
    }

    private Node makeNode(int id) {
        return new Node(
                new Task(
                        id,
                        Integer.toString(id),
                        Integer.toString(id)
                )
        );
    }

    private List<Integer> getTaskIds(List<Task> tasks) {
        return tasks.stream().map(Task::getId).toList();
    }
}
