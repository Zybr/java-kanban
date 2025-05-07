package models;

import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import models.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TaskManager {
    private int lastTaskId;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, EpicTask> epicTasks;
    private final HashMap<Integer, SubTask> subTasks;

    public TaskManager() {
        this.lastTaskId = 0;
        this.tasks = new HashMap<>();
        this.epicTasks = new HashMap<>();
        this.subTasks = new HashMap<>();
    }

    // List getters >>>

    public ArrayList<Task> getTasks() {
        return tasks.values()
                .stream()
                .map(Task::copy) // Return copies to avoid changing by link
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<EpicTask> getEpicTasks() {
        return epicTasks.values()
                .stream()
                .map(EpicTask::copy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<SubTask> getSubTasks() {
        return subTasks.values()
                .stream()
                .map(SubTask::copy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // <<< List getters

    // List removers >>>

    public void removeTasks() {
        tasks.clear();
    }

    public void removeEpicTasks() {
        epicTasks.clear();
        subTasks.clear();
    }

    public void removeSubTasks() {
        subTasks.clear();
    }

    // <<< List removers

    // One model getters >>>

    public Task getTask(int id) {
        return tasks.containsKey(id) ? tasks.get(id).copy() : null; // Provide a copy to avoid changing by link
    }

    public EpicTask getEpicTask(int id) {
        return epicTasks.containsKey(id) ? epicTasks.get(id).copy() : null;
    }

    public SubTask getSubTask(int id) {
        return subTasks.containsKey(id) ? subTasks.get(id).copy() : null;
    }

    // <<< One model getters

    // Common methods >>>

    public void removeTask(int id) {
        if (tasks.remove(id) != null) {
            return;
        }

        EpicTask epicTask = epicTasks.remove(id);

        if (epicTask != null) {
            getSubTasksOfEpic(epicTask.getId())
                    .forEach(subTask -> subTasks.remove(subTask.getId()));
        }

        SubTask result = subTasks.remove(id);

        if (result != null) {
            updateEpicTaskStatus(result.getEpicId());
        }
    }

    // <<< Common methods

    /* Overloaded methods >>> */

    /* Overloaded methods. Creation. */

    public void createTask(Task attributes) {
        int id = makeId();
        tasks.put(id, new Task( // Save a copy to avoid changing by link
                id,
                attributes.getName(),
                attributes.getDescription()
        ));
    }

    public void createTask(EpicTask attributes) {
        int id = makeId();
        epicTasks.put(id, new EpicTask(
                id,
                attributes.getName(),
                attributes.getDescription()
        ));
    }

    public void createTask(SubTask attributes) {
        int id = makeId();
        subTasks.put(id, new SubTask(
                id,
                attributes.getEpicId(),
                attributes.getName(),
                attributes.getDescription()
        ));
        updateEpicTaskStatus(attributes.getEpicId());
    }

    /* Overloaded methods. Updating. */

    public void updateTask(Task attributes) {
        tasks.get(attributes.getId())
                .fill(attributes);
    }

    public void updateTask(EpicTask attributes) {
        epicTasks.get(attributes.getId())
                .fill(attributes);
    }

    public void updateTask(SubTask attributes) {
        SubTask subTask = subTasks.get(attributes.getId());
        subTask.fill(attributes);
        updateEpicTaskStatus(subTask.getEpicId());
    }

    /* <<< Overloaded methods */

    public ArrayList<SubTask> getSubTasksOfEpic(int epicId) {
        return getSubTasks()
                .stream()
                .filter(subTask -> subTask.getEpicId() == epicId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void updateEpicTaskStatus(int epicId) {
        EpicTask epicTask = epicTasks.get(epicId);
        epicTask.setStatus(calculateStatus(epicId));
    }

    private TaskStatus calculateStatus(int epicId) {
        ArrayList<SubTask> subTasks = getSubTasksOfEpic(epicId);
        boolean hasTasks = !subTasks.isEmpty();
        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasInProgress = false;

        for (SubTask subTask : subTasks) {
            switch (subTask.getStatus()) {
                case NEW -> hasNew = true;
                case DONE -> hasDone = true;
                case IN_PROGRESS -> hasInProgress = true;
            }
        }

        if (hasInProgress || (hasNew && hasDone)) { // Mixes statuses
            return TaskStatus.IN_PROGRESS;
        }

        if (hasNew) { // Only new ones
            return TaskStatus.NEW;
        }

        if (hasDone) { // Only done ones
            return TaskStatus.DONE;
        }

        return !hasTasks ? TaskStatus.NEW : TaskStatus.IN_PROGRESS; // That's if there aren't tasks
    }

    private int makeId() {
        return ++this.lastTaskId;
    }
}
