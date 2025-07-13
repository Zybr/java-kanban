package models.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class EpicTask extends Task {
    private LocalDateTime endTime = LocalDateTime.MIN;

    public EpicTask(
            int id,
            String name,
            String description
    ) {
        super(id, name, description);
    }

    public EpicTask(
            int id,
            String name,
            String description,
            LocalDateTime startTime,
            Duration duration,
            LocalDateTime endTime
    ) {
        super(id, name, description, startTime, duration);
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void fill(EpicTask task) {
        this.setName(task.getName());
        this.setDescription(task.getDescription());
    }

    public EpicTask copy() {
        EpicTask epicTask = new EpicTask(
                getId(),
                getName(),
                getDescription(),
                getStartTime(),
                getDuration(),
                getEndTime()
        );
        epicTask.setStatus(getStatus());

        return epicTask;
    }
}
