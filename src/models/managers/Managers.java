package models.managers;

import models.history.HistoryManager;
import models.history.InMemoryHistoryManager;
import models.managers.filebacked.FileBackedTaskManager;
import models.managers.filebacked.exceptions.ManagerLoadException;
import models.managers.inmemory.InMemoryTaskManager;

import java.io.File;

public class Managers {
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public FileBackedTaskManager getfileBackedTaskManager() throws ManagerLoadException {
        return new FileBackedTaskManager(
                System.getProperty("java.io.tmpdir") // That's more configurable than using "File.createTempFile"
                        + File.separator
                        + makeShortFileName()
        );
    }

    public FileBackedTaskManager getfileBackedTaskManager(String fullFileName) throws ManagerLoadException {
        return new FileBackedTaskManager(fullFileName);
    }

    private String makeShortFileName() {
        return String.format("tasks_%d.txt", System.currentTimeMillis());
    }
}
