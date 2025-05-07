package models.tasks;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(
            int id,
            int epicId,
            String name,
            String description
    ) {
        super(id, name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void fill(SubTask task) {
        this.setName(task.getName());
        this.setDescription(task.getDescription());
        this.setStatus(task.getStatus());
    }

    public SubTask copy() {
        SubTask subTask = new SubTask(
                getId(),
                getEpicId(),
                getName(),
                getDescription()
        );
        subTask.setStatus(getStatus());

        return subTask;
    }
}
