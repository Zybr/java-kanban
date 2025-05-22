package models.managers;

import models.history.HistoryManager;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import models.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int lastTaskId;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, EpicTask> epicTasks;
    private final HashMap<Integer, SubTask> subTasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.lastTaskId = 0;
        this.tasks = new HashMap<>();
        this.epicTasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    // List getters >>>

    @Override
    public ArrayList<Task> getTasks() {
        return tasks.values()
                .stream()
                .map(Task::copy) // Return copies to avoid changing by link
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<EpicTask> getEpicTasks() {
        return epicTasks.values()
                .stream()
                .map(EpicTask::copy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return subTasks.values()
                .stream()
                .map(SubTask::copy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // <<< List getters

    // List removers >>>

    @Override
    public void removeTasks() {
        tasks.clear();
    }

    @Override
    public void removeEpicTasks() {
        epicTasks.clear();
        subTasks.clear();
    }

    @Override
    public void removeSubTasks() {
        subTasks.clear();
        getEpicTasks().forEach(epicTask -> updateEpicTaskStatus(epicTask.getId()));
    }

    // <<< List removers

    // One model getters >>>

    @Override
    public Task getTask(int id) {
        Task task = tasks.containsKey(id) ? tasks.get(id).copy() : null; // Provide a copy to avoid changing by link
        historyManager.add(task);
        return task;
    }

    @Override
    public EpicTask getEpicTask(int id) {
        EpicTask task = epicTasks.containsKey(id) ? epicTasks.get(id).copy() : null;
        historyManager.add(task);
        return task;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask task = subTasks.containsKey(id) ? subTasks.get(id).copy() : null;
        historyManager.add(task);
        return task;
    }

    // <<< One model getters

    // Common methods >>>

    @Override
    public void removeTask(int id) {
        if (tasks.remove(id) != null) {
            return;
        }

        EpicTask epicTask = epicTasks.remove(id);

        if (epicTask != null) {
            getEpicSubTasks(epicTask.getId())
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

    @Override
    public Task createTask(Task attributes) {
        int id = makeId();
        tasks.put(id, new Task( // Save a copy to avoid changing by link
                id,
                attributes.getName(),
                attributes.getDescription()
        ));

        return tasks.get(id).copy();
    }

    @Override
    public EpicTask createTask(EpicTask attributes) {
        int id = makeId();
        epicTasks.put(id, new EpicTask(
                id,
                attributes.getName(),
                attributes.getDescription()
        ));

        return epicTasks.get(id).copy();
    }

    @Override
    public SubTask createTask(SubTask attributes) {
        int id = makeId();
        subTasks.put(id, new SubTask(
                id,
                attributes.getEpicId(),
                attributes.getName(),
                attributes.getDescription()
        ));
        updateEpicTaskStatus(attributes.getEpicId());

        return subTasks.get(id).copy();
    }

    /* Overloaded methods. Updating. */

    @Override
    public void updateTask(Task attributes) {
        tasks.get(attributes.getId())
                .fill(attributes);
    }

    @Override
    public void updateTask(EpicTask attributes) {
        epicTasks.get(attributes.getId())
                .fill(attributes);
    }

    @Override
    public void updateTask(SubTask attributes) {
        SubTask subTask = subTasks.get(attributes.getId());
        subTask.fill(attributes);
        updateEpicTaskStatus(subTask.getEpicId());
    }

    /* <<< Overloaded methods */

    @Override
    public ArrayList<SubTask> getEpicSubTasks(int epicId) {
        return getSubTasks()
                .stream()
                .filter(subTask -> subTask.getEpicId() == epicId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicTaskStatus(int epicId) {
        EpicTask epicTask = epicTasks.get(epicId);
        epicTask.setStatus(calculateStatus(epicId));
    }

    private TaskStatus calculateStatus(int epicId) {
        ArrayList<SubTask> subTasks = getEpicSubTasks(epicId);
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
