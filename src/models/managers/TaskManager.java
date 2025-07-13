package models.managers;

import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Task> getPrioritizedTasks();

    ArrayList<EpicTask> getEpicTasks();

    ArrayList<SubTask> getSubTasks();

    void removeTasks();

    void removeEpicTasks();

    void removeSubTasks();

    Optional<Task> getTask(int id);

    Optional<EpicTask> getEpicTask(int id);

    Optional<SubTask> getSubTask(int id);

    void removeTask(int id);

    Task createTask(Task attributes);

    EpicTask createTask(EpicTask attributes);

    SubTask createTask(SubTask attributes);

    void updateTask(Task attributes);

    void updateTask(EpicTask attributes);

    void updateTask(SubTask attributes);

    ArrayList<SubTask> getEpicSubTasks(int epicId);

    List<Task> getHistory();

    boolean isEmpty();

    void removeAllTasks();
}
