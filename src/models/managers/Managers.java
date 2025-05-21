package models.managers;

import models.history.HistoryManager;
import models.history.InMemoryHistoryManager;

public class Managers {
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
