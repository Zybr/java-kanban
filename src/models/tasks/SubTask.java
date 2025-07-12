package models.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(
            int id,
            int epicId,
            String name,
            String description,
            LocalDateTime startTime,
            Duration duration
    ) {
        super(id, name, description, startTime, duration);
        this.epicId = epicId;
    }

    /**
     * "Внутри эпиков не должно оставаться неактуальных id подзадач."
     */
    public int getEpicId() {
        return epicId;
    }

    public void fill(SubTask task) {
        this.setName(task.getName());
        this.setDescription(task.getDescription());
        this.setStatus(task.getStatus());
        this.setStartTime(task.getStartTime());
        this.setDuration(task.getDuration());
    }

    public SubTask copy() {
        SubTask subTask = new SubTask(
                getId(),
                getEpicId(),
                getName(),
                getDescription(),
                getStartTime(),
                getDuration()
        );
        subTask.setStatus(getStatus());

        return subTask;
    }
}
