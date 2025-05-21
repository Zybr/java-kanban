package models.history;

import models.tasks.Task;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class InMemoryHistoryManager implements HistoryManager {
    private final static int HISTORY_MAX_SIZE = 10;
    private final ArrayList<Task> tasks = new ArrayList<>();

    @Override
    public ArrayList<Task> getHistory() {
        return tasks
                .stream()
                .map(Task::copy) // Return copies to avoid changing by link
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        this.tasks.add(task.copy());

        if (this.tasks.size() > HISTORY_MAX_SIZE) {
            this.tasks.removeFirst();
        }
    }
}
