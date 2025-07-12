package models.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    private final int id;
    private String name;

    private String description;
    private TaskStatus status;
    private LocalDateTime startTime = LocalDateTime.MIN;
    private Duration duration = Duration.ZERO;

    public Task(
            int id,
            String name,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public Task(
            int id,
            String name,
            String description,
            LocalDateTime startTime,
            Duration duration
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        return this.startTime.plus(duration);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Task
                && id == ((Task) object).getId();
    }

    /**
     * Fill the Task by values of another Task
     */
    public void fill(Task task) {
        this.setName(task.getName());
        this.setDescription(task.getDescription());
        this.setStartTime(task.getStartTime());
        this.setDuration(task.getDuration());
    }

    /**
     * Make copy of itself
     */
    public Task copy() {
        Task subTask = new Task(
                getId(),
                getName(),
                getDescription(),
                getStartTime(),
                getDuration()
        );
        subTask.setStatus(getStatus());

        return subTask;
    }

    @Override
    public String toString() {
        return String.format(
                "%d;%s;%s-%s",
                getId(),
                getName(),
                getStartTime().toString(),
                getEndTime().toString()
        );
    }
}
