package models.history;

import models.tasks.Task;

import java.util.ArrayList;

public interface HistoryManager {
    void add(Task task);

    void remove(int taskId);

    ArrayList<Task> getHistory();
}
