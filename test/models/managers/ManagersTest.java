package models.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @see Managers
 */
class ManagersTest {
    /**
     * "Убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;"
     *
     * @see Managers#getDefault()
     */
    @Test
    public void shouldGetDefaultManger() {
        TaskManager manager = new Managers().getDefault();

        assertInstanceOf(InMemoryTaskManager.class, manager);
        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }
}