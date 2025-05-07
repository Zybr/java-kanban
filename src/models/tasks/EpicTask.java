package models.tasks;

public class EpicTask extends Task {
    public EpicTask(
            int id,
            String name,
            String description
    ) {
        super(id, name, description);
    }

    public void fill(EpicTask task) {
        this.setName(task.getName());
        this.setDescription(task.getDescription());
    }

    public EpicTask copy() {
        EpicTask epicTask = new EpicTask(
                getId(),
                getName(),
                getDescription()
        );
        epicTask.setStatus(getStatus());

        return epicTask;
    }
}
