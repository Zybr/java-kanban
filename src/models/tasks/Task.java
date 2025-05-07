package models.tasks;

public class Task {
    private final int id;
    private String name;
    private String description;
    private TaskStatus status;

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
        this.setStatus(task.getStatus());
    }

    /**
     * Make copy of itself
     */
    public Task copy() {
        Task subTask = new Task(
                getId(),
                getName(),
                getDescription()
        );
        subTask.setStatus(getStatus());

        return subTask;
    }
}
