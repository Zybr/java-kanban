package models.managers.inmemory;

import models.managers.AbstractTaskManagerTest;
import models.managers.Managers;
import models.managers.TaskManager;

/**
 * @see InMemoryTaskManager
 */
public class InMemoryTaskManagerTest extends AbstractTaskManagerTest {
    @Override
    protected TaskManager makeManager() {
        return new Managers().getDefault();
    }
}