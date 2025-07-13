package models.managers.inmemory;

import models.history.HistoryManager;
import models.managers.Managers;
import models.managers.TaskManager;
import models.tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int lastTaskId;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, EpicTask> epicTasks;
    private final HashMap<Integer, SubTask> subTasks;
    private final HistoryManager historyManager;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
    // Remove tasks one by one to execute extra logic such as history updating

    @Override
    public void removeTasks() {
        getTasks().forEach(task -> removeTask(task.getId()));
    }

    @Override
    public void removeEpicTasks() {
        getEpicTasks().forEach(epicTask -> removeTask(epicTask.getId()));
    }

    @Override
    public void removeSubTasks() {
        getSubTasks().forEach(subTask -> removeTask(subTask.getId()));
    }

    // <<< List removers

    // One model getters >>>

    @Override
    public Optional<Task> getTask(int id) {
        Optional<Task> task = tasks.containsKey(id) ? Optional.of(tasks.get(id)) : Optional.empty();
        task.ifPresent(historyManager::add);
        return task;
    }

    @Override
    public Optional<EpicTask> getEpicTask(int id) {
        Optional<EpicTask> task = epicTasks.containsKey(id) ? Optional.of(epicTasks.get(id)) : Optional.empty();
        task.ifPresent(historyManager::add);
        return task;
    }

    @Override
    public Optional<SubTask> getSubTask(int id) {
        Optional<SubTask> task = subTasks.containsKey(id) ? Optional.of(subTasks.get(id)) : Optional.empty();
        task.ifPresent(historyManager::add);
        return task;
    }

    // <<< One model getters

    // Common methods >>>

    @Override
    public void removeTask(int id) {
        Task regularTask = tasks.remove(id);

        if (regularTask != null) {
            historyManager.remove(regularTask.getId());
            deprioritizeTask(regularTask);
            return;
        }

        EpicTask epicTask = epicTasks.remove(id);

        if (epicTask != null) {
            getEpicSubTasks(epicTask.getId())
                    .forEach(subTask -> {
                        subTasks.remove(subTask.getId());
                        historyManager.remove(subTask.getId());
                        deprioritizeTask(subTask);
                    });
            historyManager.remove(epicTask.getId());
        }

        SubTask subTask = subTasks.remove(id);

        if (subTask != null) {
            updateEpicTask(subTask.getEpicId());
            historyManager.remove(subTask.getId());
            deprioritizeTask(subTask);
        }
    }

    public void removeAllTasks() {
        removeTasks();
        removeEpicTasks();
    }

    public boolean isEmpty() {
        return this.getTasks().isEmpty() && this.getEpicTasks().isEmpty();
    }

    // <<< Common methods

    /* Overloaded methods >>> */

    /* Overloaded methods. Creation. */

    @Override
    public Task createTask(Task attributes) {
        Task task = new Task(
                makeId(),
                attributes.getName(),
                attributes.getDescription(),
                attributes.getStartTime(),
                attributes.getDuration()
        );

        checkIntersection(task);

        tasks.put(task.getId(), task);
        this.prioritizeTask(task);

        // Save a copy to avoid changing by link
        return task.copy();
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
        SubTask subTask = new SubTask(
                makeId(),
                attributes.getEpicId(),
                attributes.getName(),
                attributes.getDescription(),
                attributes.getStartTime(),
                attributes.getDuration()
        );

        checkIntersection(subTask);

        subTasks.put(subTask.getId(), subTask);
        updateEpicTask(attributes.getEpicId());
        this.prioritizeTask(subTask);

        return subTask.copy();
    }

    /* Overloaded methods. Updating. */

    @Override
    public void updateTask(Task attributes) {
        checkIntersection(attributes);

        tasks.get(attributes.getId())
                .fill(attributes);
        this.prioritizeTask(tasks.get(attributes.getId()));
    }

    @Override
    public void updateTask(EpicTask attributes) {
        epicTasks.get(attributes.getId())
                .fill(attributes);
    }

    @Override
    public void updateTask(SubTask attributes) {
        checkIntersection(attributes);

        SubTask subTask = subTasks.get(attributes.getId());
        subTask.fill(attributes);
        this.prioritizeTask(subTask);
        updateEpicTask(subTask.getEpicId());
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

    private void updateEpicTask(int epicId) {
        EpicTask epicTask = epicTasks.get(epicId);
        epicTask.setStatus(calculateEpicStatus(epicId));
        epicTask.setStartTime(calculateEpicStartTime(epicId));
        epicTask.setDuration(calculateEpicDuration(epicId));
    }

    private int makeId() {
        return ++this.lastTaskId;
    }

    private TaskStatus calculateEpicStatus(int epicId) {
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

    private LocalDateTime calculateEpicStartTime(int epicId) {
        return getEpicSubTasks(epicId)
                .stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }

    private Duration calculateEpicDuration(int epicId) {
        return getEpicSubTasks(epicId)
                .stream()
                .reduce(
                        Duration.ZERO,
                        (duration, task) -> duration.plus(task.getDuration()),
                        (Duration::plus)
                );
    }

    private LocalDateTime calculateEpicEndTime(int epicId) {
        return getEpicSubTasks(epicId)
                .stream()
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(calculateEpicStartTime(epicId));
    }

    private void prioritizeTask(Task task) {
        this.deprioritizeTask(task);

        if (
                !task.getStartTime().isEqual(LocalDateTime.MIN)
                        && !task.getDuration().isZero()         // <--
        ) {
            this.prioritizedTasks.add(task);
        }
    }

    private void deprioritizeTask(Task task) {
        this.prioritizedTasks.remove(task);
    }

    /* Tasks intersection >>> */

    private void checkIntersection(Task task) {
        getPrioritizedTasks()
                .stream()
                .filter(
                        (priorTask) -> priorTask.getId() != task.getId()
                                && areIntersectingByPeriod(priorTask, task)
                )
                .findFirst()
                .ifPresent((priorTask) -> {
                    throw new IllegalArgumentException(
                            String.format(
                                    "The task \"%s\" is intersecting with \"%s\"",
                                    task,
                                    priorTask
                            )
                    );
                });
    }

    private boolean areIntersectingByPeriod(Task task1, Task task2) {
        LocalDateTime s1 = task1.getStartTime();
        LocalDateTime e1 = task1.getEndTime();
        LocalDateTime s2 = task2.getStartTime();
        LocalDateTime e2 = task2.getEndTime();

        return (isInsidePeriod(s1, e1, s2) || s2.isEqual(s1))
                || (isInsidePeriod(s1, e1, e2))
                || (isInsidePeriod(s2, e2, s1) || s1.isEqual(s2))
                || (isInsidePeriod(s2, e2, e2));
    }

    private boolean isInsidePeriod(
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            LocalDateTime timePoint
    ) {
        return timePoint.isAfter(rangeStart) && timePoint.isBefore(rangeEnd);
    }

    /* <<< Tasks intersection */
}