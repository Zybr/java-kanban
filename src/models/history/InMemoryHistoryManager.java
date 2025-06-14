package models.history;

import models.history.nodes_list.Node;
import models.history.nodes_list.NodesList;
import models.tasks.Task;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryHistoryManager implements HistoryManager {
    private final NodesList<Task> nodes = new NodesList<>();
    private final Map<Integer, Node<Task>> taskNodes = new HashMap<>();

    @Override
    public ArrayList<Task> getHistory() {
        return nodes
                .getValues()
                .stream()
                .map(Task::copy) // Return copies to avoid changing by link
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());
        taskNodes.put(
                task.getId(),
                this.nodes.push(new Node<>(task.copy()))
        );
    }

    @Override
    public void remove(int taskId) {
        Node<Task> node = taskNodes.remove(taskId);

        if (node != null) {
            nodes.remove(node);
        }
    }
}
