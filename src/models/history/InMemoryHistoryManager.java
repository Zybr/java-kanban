package models.history;

import models.history.tasks_list.TaskNode;
import models.history.tasks_list.TaskNodesList;
import models.tasks.Task;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryHistoryManager implements HistoryManager {
    private final TaskNodesList nodes = new TaskNodesList();
    private final Map<Integer, TaskNode> taskNodes = new HashMap<>();

    @Override
    public ArrayList<Task> getHistory() {
        return nodes
                .getTasks()
                .stream()
                .map(Task::copy) // Return copies to avoid changing by link
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task);
        taskNodes.put(
                task.getId(),
                this.nodes.addTask(task.copy())
        );
    }

    @Override
    public void remove(Task task) {
        TaskNode node = taskNodes.remove(task.getId());

        if (node != null) {
            nodes.removeNode(node);
        }
    }
}
