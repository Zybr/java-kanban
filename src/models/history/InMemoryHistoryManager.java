package models.history;

import models.tasks.Task;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Task> tasks = new LinkedHashMap<>();

    @Override
    public ArrayList<Task> getHistory() {
        return tasks
                .values()
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
        this.tasks.put(task.getId(), task.copy());
    }

    @Override
    public void remove(Task task) {
        tasks.remove(task.getId());
    }
}
