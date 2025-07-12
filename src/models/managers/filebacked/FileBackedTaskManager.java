package models.managers.filebacked;

import models.managers.filebacked.exceptions.ManagerLoadException;
import models.managers.filebacked.exceptions.ManagerSaveException;
import models.managers.inmemory.InMemoryTaskManager;
import models.tasks.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final Charset fileEncoding = StandardCharsets.UTF_8;
    private static final String csvColumnSeparator = ",";
    private final String fullFileName;

    public FileBackedTaskManager(String fullFileName) {
        this.fullFileName = fullFileName;
    }

    public Task createTask(Task attributes) {
        Task task = super.createTask(attributes);
        save();
        return task;
    }

    @Override
    public EpicTask createTask(EpicTask attributes) {
        EpicTask subTask = super.createTask(attributes);
        save();
        return subTask;
    }

    @Override
    public SubTask createTask(SubTask attributes) {
        SubTask subTask = super.createTask(attributes);
        save();
        return subTask;
    }

    @Override
    public void updateTask(Task attributes) {
        super.updateTask(attributes);
        save();
    }

    @Override
    public void updateTask(EpicTask attributes) {
        super.updateTask(attributes);
        save();
    }

    @Override
    public void updateTask(SubTask attributes) {
        super.updateTask(attributes);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    public String getFullFileName() {
        return fullFileName;
    }

    private void save() {
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

    public static FileBackedTaskManager loadFromFile(File file) {
        Path filePath = Paths.get(file.getPath());

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());

        if (Files.notExists(filePath)) {
            return manager;
        }

        try {
            String csvRowSeparator = "\n";
            Arrays.stream(
                            Files
                                    .readString(filePath, fileEncoding)
                                    .split(csvRowSeparator)
                    )
                    .forEach(line -> {
                        Task task = deserializeTask(line);
                        switch (getTaskType(task)) {
                            case TaskType.REGULAR -> manager.createTask(task);
                            case TaskType.SUB -> manager.createTask((SubTask) task);
                            case TaskType.EPIC -> manager.createTask((EpicTask) task);
                        }
                    });
        } catch (Exception e) {
            throw new ManagerLoadException(e.getMessage());
        }

        return manager;
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
     * - 6: Timestamp of start time
     * - 7: Minutes of duration
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
                        getEpicReference(task),
                        String.valueOf(task.getStartTime().toEpochSecond(ZoneOffset.UTC)),
                        String.valueOf(task.getDuration().toMinutes())
                });
    }

    private static Task deserializeTask(String serializedTask) {
        String[] attributes = serializedTask.trim().split(csvColumnSeparator, 8);

        int id = Integer.parseInt(attributes[0]);
        TaskType type = TaskType.valueOf(attributes[1]);
        String name = attributes[2];
        TaskStatus status = TaskStatus.valueOf(attributes[3]);
        String description = attributes[4];
        int epicId = attributes[5].isEmpty() ? 0 : Integer.parseInt(attributes[5]);
        LocalDateTime startTime = LocalDateTime.ofEpochSecond(
                attributes[6].isEmpty() ? 0 : Long.parseLong(attributes[6]),
                0,
                ZoneOffset.UTC
        );
        Duration duration = Duration.ofMinutes(
                attributes[7].isEmpty() ? 0 : Integer.parseInt(attributes[7])
        );

        Task task = switch (type) {
            case TaskType.REGULAR -> new Task(id, name, description, startTime, duration);
            case TaskType.SUB -> new SubTask(id, epicId, name, description, startTime, duration);
            case TaskType.EPIC -> new EpicTask(id, name, description);
        };

        task.setStatus(status);

        return task;
    }

    private static TaskType getTaskType(Task task) {
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
