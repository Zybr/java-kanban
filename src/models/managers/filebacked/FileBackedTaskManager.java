package models.managers.filebacked;

import models.managers.inmemory.InMemoryTaskManager;
import models.managers.TaskManager;
import models.managers.filebacked.exceptions.ManagerLoadException;
import models.managers.filebacked.exceptions.ManagerSaveException;
import models.tasks.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final Charset fileEncoding = StandardCharsets.UTF_8;
    private final String csvColumnSeparator = ",";
    private final String fullFileName;

    public FileBackedTaskManager(String fullFileName) throws ManagerLoadException {
        this.fullFileName = fullFileName;
        load();
    }

    public Task createTask(Task attributes) throws ManagerSaveException {
        Task task = super.createTask(attributes);
        save();
        return task;
    }

    @Override
    public EpicTask createTask(EpicTask attributes) throws ManagerSaveException {
        EpicTask subTask = super.createTask(attributes);
        save();
        return subTask;
    }

    @Override
    public SubTask createTask(SubTask attributes) throws ManagerSaveException {
        SubTask subTask = super.createTask(attributes);
        save();
        return subTask;
    }

    @Override
    public void updateTask(Task attributes) throws ManagerSaveException {
        super.updateTask(attributes);
        save();
    }

    @Override
    public void updateTask(EpicTask attributes) throws ManagerSaveException {
        super.updateTask(attributes);
        save();
    }

    @Override
    public void updateTask(SubTask attributes) throws ManagerSaveException {
        super.updateTask(attributes);
        save();
    }

    @Override
    public void removeTask(int id) throws ManagerSaveException {
        super.removeTask(id);
        save();
    }

    public String getFullFileName() {
        return fullFileName;
    }

    private void save() throws ManagerSaveException {
        try (
                BufferedWriter bufferedWriter = new BufferedWriter(
                        new FileWriter(
                                fullFileName,
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            for (Task task : getTasks()) writeTask(bufferedWriter, task);
            for (EpicTask task : getEpicTasks()) writeTask(bufferedWriter, task);
            for (SubTask task : getSubTasks()) writeTask(bufferedWriter, task);
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        if (isEmpty()) { // Remove the file if it's empty, just to not spam the folder by empty files
            try {
                Files.deleteIfExists(Paths.get(fullFileName));
            } catch (IOException e) {
                throw new ManagerSaveException(e.getMessage());
            }
        }
    }

    private void load() throws ManagerLoadException {
        Path filePath = Paths.get(fullFileName);

        if (Files.notExists(filePath)) {
            return;
        }

        try {
            String csvRowSeparator = "\n";
            for (String line : Files.readString(filePath, fileEncoding).split(csvRowSeparator)) {
                Task task = deserializeTask(line);
                switch (getTaskType(task)) {
                    case TaskType.REGULAR -> createTask(task);
                    case TaskType.SUB -> createTask((SubTask) task);
                    case TaskType.EPIC -> createTask((EpicTask) task);
                }
            }
        } catch (Exception e) {
            throw new ManagerLoadException();
        }
    }

    private void writeTask(BufferedWriter writer, Task task) throws IOException {
        String line = serializeTask(task);
        writer.write(line);
        writer.newLine();
    }

    /**
     * Make CSV line of values:
     * - 0: ID
     * - 1: Type
     * - 2: Name
     * - 3: Status
     * - 4: Description
     * - 5: Epic ID
     */
    private String serializeTask(Task task) {
        return String.join(
                csvColumnSeparator,
                new String[]{
                        String.valueOf(task.getId()),
                        getTaskType(task).name(),
                        task.getName(),
                        task.getStatus().name(),
                        task.getDescription(),
                        getEpicReference(task)
                });
    }

    private Task deserializeTask(String serializedTask) throws ManagerSaveException {
        String[] attributes = serializedTask.split(csvColumnSeparator, 6);

        int id = Integer.parseInt(attributes[0]);
        TaskType type = TaskType.valueOf(attributes[1]);
        String name = attributes[2];
        TaskStatus status = TaskStatus.valueOf(attributes[3]);
        String description = attributes[4];
        int epicId = attributes[5].isEmpty() ? 0 : Integer.parseInt(attributes[5]);

        Task task = switch (type) {
            case TaskType.REGULAR -> new Task(id, name, description);
            case TaskType.SUB -> new SubTask(id, epicId, name, description);
            case TaskType.EPIC -> new EpicTask(id, name, description);
        };

        task.setStatus(status);

        return task;
    }

    private TaskType getTaskType(Task task) {
        return switch (task) {
            case EpicTask epicTask -> TaskType.EPIC;
            case SubTask subTask -> TaskType.SUB;
            case Task regularTask -> TaskType.REGULAR;
        };
    }

    private String getEpicReference(Task task) {
        return task instanceof SubTask ? String.valueOf(((SubTask) task).getEpicId()) : "";
    }
}
