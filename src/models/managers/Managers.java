package models.managers;

import models.history.HistoryManager;
import models.history.InMemoryHistoryManager;
import models.managers.filebacked.FileBackedTaskManager;
import models.managers.inmemory.InMemoryTaskManager;

import java.io.File;

public class Managers {
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public FileBackedTaskManager getfileBackedTaskManager() {
        return getfileBackedTaskManager(makeFullFileName());
    }

    public FileBackedTaskManager getfileBackedTaskManager(String fullFileName) {
        return FileBackedTaskManager.loadFromFile(new File(fullFileName));
    }

    private String makeFullFileName() {
        return System.getProperty("java.io.tmpdir") // That's more configurable than using "File.createTempFile"
                + File.separator
                + makeShortFileName();
    }

    private String makeShortFileName() {
        return String.format("tasks_%d.txt", System.currentTimeMillis());
    }
}
