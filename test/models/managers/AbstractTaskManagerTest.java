package models.managers;

import models.factories.TasksFactory;
import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;
import models.tasks.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @see TaskManager
 */
public abstract class AbstractTaskManagerTest {
    protected TaskManager manager;

    protected abstract TaskManager makeManager();

    @BeforeEach
    public void initManager() {
        manager = makeManager();
    }

    /**
     * @see TaskManager#createTask(Task)
     * @see TaskManager#updateTask(Task)
     * @see TaskManager#removeTask(int)
     */
    @Test
    public void shouldManageRegularTasks() {
        // Creation
        Task taskA = TasksFactory.makeTask();
        Task taskB = TasksFactory.makeTask();
        manager.createTask(taskA);
        manager.createTask(taskB);
        ArrayList<Task> tasks = manager.getTasks();
        assertEquals(2, tasks.size());
        assertTaskContent(tasks.get(0), 1, taskA.getName());
        assertTaskContent(tasks.get(1), 2, taskB.getName());

        // Updating
        Task task = tasks.get(0);
        task.setName(task.getName() + " [updated]");
        manager.updateTask(task);
        assertTaskContent(tasks.getFirst(), 1, task.getName());

        // Removing
        manager.removeTask(task.getId());
        assertEquals(1, manager.getTasks().size());
    }

    /**
     * @see TaskManager#createTask(EpicTask)
     * @see TaskManager#updateTask(SubTask)
     * @see TaskManager#removeTask(int)
     */
    @Test
    public void shouldEpicStatusDependOnSubs() {
        EpicTask epic = manager.createTask(TasksFactory.makeEpic());
        assertEquals(TaskStatus.NEW, epic.getStatus());

        // Subs is NEW + Sub is DONE = Epic is IN PROGRESS
        SubTask subA = manager.createTask(TasksFactory.makeSub(epic.getId()));
        SubTask subB = manager.createTask(TasksFactory.makeSub(epic.getId()));
        assertEquals(TaskStatus.NEW, manager.getEpicTask(epic.getId()).orElseThrow().getStatus());

        // Sub is IN PROGRESS + Sub is DONE = Epic is IN PROGRESS
        subA.setStatus(TaskStatus.DONE);
        manager.updateTask(subA);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTask(epic.getId()).orElseThrow().getStatus());

        // Sub is DONE + Sub DONE = Epic is DONE
        subB.setStatus(TaskStatus.DONE);
        manager.updateTask(subB);
        assertEquals(TaskStatus.DONE, manager.getEpicTask(epic.getId()).orElseThrow().getStatus());

        // No Subs = Epic is NEW
        manager.removeSubTasks();
        assertEquals(TaskStatus.NEW, manager.getEpicTask(epic.getId()).orElseThrow().getStatus());
    }


    /**
     * @see TaskManager#createTask(EpicTask)
     * @see TaskManager#createTask(SubTask)
     * @see TaskManager#updateTask(SubTask)
     * @see TaskManager#removeTask(int)
     */
    @Test
    public void shouldEpicStartTimeDependOnSubs() {
        // Empty Epic
        EpicTask epic = manager.createTask(TasksFactory.makeEpic());
        assertEquals(LocalDateTime.MIN, epic.getStartTime());

        // Epic with 1 Sub
        SubTask subA = manager.createTask(TasksFactory.makeSub(epic.getId()));
        assertEquals(
                subA.getStartTime(),
                manager.getEpicTask(epic.getId()).orElseThrow().getStartTime()
        );

        // Epic with 2 Subs
        SubTask subB = TasksFactory.makeSub(epic.getId());
        subB.setStartTime(LocalDateTime.now().minusDays(3));
        subB = manager.createTask(subB);
        assertEquals(
                subB.getStartTime(),
                manager.getEpicTask(epic.getId()).orElseThrow().getStartTime()
        );

        // Update Sub
        subB.setStartTime(LocalDateTime.now().minusDays(1));
        manager.updateTask(subB);
        assertEquals(
                subB.getStartTime(),
                manager.getEpicTask(epic.getId()).orElseThrow().getStartTime()
        );

        // Remove Sub
        manager.removeTask(subB.getId());
        assertEquals(
                subA.getStartTime(),
                manager.getEpicTask(epic.getId()).orElseThrow().getStartTime()
        );
    }

    /**
     * @see TaskManager#createTask(EpicTask)
     * @see TaskManager#createTask(SubTask)
     * @see TaskManager#updateTask(SubTask)
     * @see TaskManager#removeTask(int)
     */
    @Test
    public void shouldEpicDurationDependOnSubs() {
        // Empty Epic
        EpicTask epic = manager.createTask(TasksFactory.makeEpic());
        assertEquals(Duration.ZERO, epic.getDuration());

        // Epic with 1 Sub
        SubTask subA = TasksFactory.makeSub(epic.getId());
        subA.setStartTime(LocalDateTime.now().plusDays(1));
        subA = manager.createTask(subA);
        assertEquals(
                subA.getDuration(),
                manager.getEpicTask(epic.getId()).orElseThrow().getDuration()
        );

        // Epic with 2 Subs
        SubTask subB = TasksFactory.makeSub(epic.getId());
        subB.setStartTime(LocalDateTime.now().plusDays(5));
        subB = manager.createTask(subB);
        assertEquals(
                Duration.between(subA.getStartTime(), subB.getEndTime()),
                manager.getEpicTask(epic.getId()).orElseThrow().getDuration()
        );

        // Move start time
        subA.setStartTime(LocalDateTime.now().minusDays(1));
        manager.updateTask(subA);
        assertEquals(
                Duration.between(subA.getStartTime(), subB.getEndTime()),
                manager.getEpicTask(epic.getId()).orElseThrow().getDuration()
        );

        // Move end time
        subB.setDuration(subB.getDuration().plusDays(1));
        manager.updateTask(subB);
        assertEquals(
                Duration.between(subA.getStartTime(), subB.getEndTime()),
                manager.getEpicTask(epic.getId()).orElseThrow().getDuration()
        );

        // Remove one Sub
        manager.removeTask(subB.getId());
        assertEquals(
                subA.getDuration(),
                manager.getEpicTask(epic.getId()).orElseThrow().getDuration()
        );

        // Remove all Subs
        manager.removeTask(subA.getId());
        assertEquals(
                Duration.ZERO,
                manager.getEpicTask(epic.getId()).orElseThrow().getDuration()
        );
    }

    /**
     * @see TaskManager#removeTask(int)
     */
    @Test
    public void shouldRemoveCascade() {
        EpicTask epic = manager.createTask(TasksFactory.makeEpic());
        manager.createTask(TasksFactory.makeSub(epic.getId()));
        manager.createTask(TasksFactory.makeSub(epic.getId()));
        assertEquals(1, manager.getEpicTasks().size());
        assertEquals(2, manager.getSubTasks().size());

        manager.removeTask(epic.getId());
        assertEquals(0, manager.getEpicTasks().size());
        assertEquals(0, manager.getSubTasks().size());
    }

    /**
     * "Проверьте, что TaskManager действительно добавляет задачи разного типа и может найти их по id;"
     *
     * @see TaskManager#createTask
     * @see TaskManager#getTask(int)
     * @see TaskManager#getEpicTask(int)
     * @see TaskManager#getSubTask(int)
     */
    @Test
    public void shouldAddTasks() {
        // Regular Tasks
        manager.createTask(TasksFactory.makeTask());
        Task regularTask = manager.getTasks().getFirst();
        Optional<Task> returnedTask = manager.getTask(regularTask.getId());
        assertTrue(returnedTask.isPresent());
        assertEquals(regularTask, returnedTask.get());

        // Epic Tasks
        manager.createTask(TasksFactory.makeEpic());
        EpicTask epicTask = manager.getEpicTasks().getFirst();
        assertEquals(
                epicTask,
                manager.getEpicTask(epicTask.getId()).orElseThrow()
        );

        // Sub Tasks
        manager.createTask(TasksFactory.makeSub(epicTask.getId()));
        SubTask subTask = manager.getSubTasks().getFirst();
        assertEquals(
                subTask,
                manager.getSubTask(subTask.getId()).orElseThrow()
        );
    }

    /**
     * "Проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;"
     *
     * @see TaskManager#createTask
     */
    @Test
    public void shouldNotConflictByIds() {
        Task taskA = manager.createTask(TasksFactory.makeTask());
        Task taskB = manager.createTask(TasksFactory.makeTask());
        ArrayList<Task> regularTasks = manager.getTasks();
        assertEquals(2, regularTasks.size());
        assertTaskContent(regularTasks.get(0), 1, taskA.getName());
        assertTaskContent(regularTasks.get(1), 2, taskB.getName());

        EpicTask epicA = manager.createTask(TasksFactory.makeEpic());
        EpicTask epicB = manager.createTask(TasksFactory.makeEpic());
        ArrayList<EpicTask> epicTasks = manager.getEpicTasks();
        assertEquals(2, epicTasks.size());
        assertTaskContent(epicTasks.get(0), 3, epicA.getName());
        assertTaskContent(epicTasks.get(1), 4, epicB.getName());

        SubTask subA = manager.createTask(TasksFactory.makeSub(epicA.getId()));
        SubTask subB = manager.createTask(TasksFactory.makeSub(epicB.getId()));
        ArrayList<SubTask> subTasks = manager.getSubTasks();
        assertEquals(2, subTasks.size());
        assertTaskContent(subTasks.get(0), 5, subA.getName());
        assertTaskContent(subTasks.get(1), 6, subB.getName());
    }


    // Changing by link >>>

    /**
     * "Создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджере"
     *
     * @see TaskManager#getTasks()
     */
    @Test
    public void shouldNotTaskBeChangeableByLink() {
        Task sourceTask = TasksFactory.makeTask();
        manager.createTask(sourceTask);

        Task originalTask = sourceTask.copy();
        sourceTask.setName(sourceTask.getName() + " [updated]");
        sourceTask.setStatus(TaskStatus.DONE);
        sourceTask.setDescription(sourceTask.getDescription() + " [updated]");

        // Tyr to change the history by link
        ArrayList<Task> tasks = manager.getTasks();
        tasks.getFirst().setName(tasks.getFirst() + " [updated]");

        assertEqualsByContent(
                originalTask,
                manager.getTasks().getFirst()
        );
    }

    /**
     * @see TaskManager#createTask(EpicTask)
     */
    @Test
    public void shouldNotEpicBeChangeableByLink() {
        EpicTask sourceTask = TasksFactory.makeEpic();
        manager.createTask(sourceTask);

        EpicTask originalTask = sourceTask.copy();
        sourceTask.setName(sourceTask.getName() + " [updated]");
        sourceTask.setStatus(TaskStatus.DONE);
        sourceTask.setDescription(sourceTask.getDescription() + " [updated]");

        assertEqualsByContent(
                originalTask,
                manager.getEpicTasks().getFirst()
        );
    }

    /**
     * @see TaskManager#createTask(SubTask)
     */
    @Test
    public void shouldNotSubBeChangeableByLink() {
        manager.createTask(TasksFactory.makeEpic());
        EpicTask epicTask = manager.getEpicTasks().getFirst();

        SubTask sourceTask = TasksFactory.makeSub(epicTask.getId());
        manager.createTask(sourceTask);
        SubTask originalTask = sourceTask.copy();

        sourceTask.setName(sourceTask.getName() + " [updated]");
        sourceTask.setStatus(TaskStatus.DONE);
        sourceTask.setDescription(sourceTask.getDescription() + " [updated]");

        assertEqualsByContent(
                originalTask,
                manager.getSubTasks().getFirst()
        );
    }

    /**
     * @see TaskManager#isEmpty()
     */
    @Test
    public void shouldCheckIfThereAreTasks() {
        assertTrue(manager.isEmpty());

        manager.createTask(TasksFactory.makeTask());
        assertFalse(manager.isEmpty());
        manager.removeTasks();
        assertTrue(manager.isEmpty());

        manager.createTask(TasksFactory.makeEpic());
        assertFalse(manager.isEmpty());
        manager.removeEpicTasks();
        assertTrue(manager.isEmpty());

        EpicTask epicTask = manager.createTask(TasksFactory.makeEpic());
        manager.createTask(TasksFactory.makeSub(epicTask.getId()));
        assertFalse(manager.isEmpty());
        manager.removeEpicTasks();
        assertTrue(manager.isEmpty());
    }

    /**
     * @see TaskManager#isEmpty()
     * @see TaskManager#removeAllTasks()
     */
    @Test
    public void shouldRemoveAllTask() {
        manager.createTask(TasksFactory.makeTask());
        EpicTask epicTask = manager.createTask(TasksFactory.makeEpic());
        manager.createTask(TasksFactory.makeSub(epicTask.getId()));
        assertFalse(manager.isEmpty());

        manager.removeAllTasks();
        assertTrue(manager.isEmpty());
    }

    @Test
    public void shouldMakeHistory() {
        assertEquals(List.of(), manager.getHistory());

        Task task1 = manager.createTask(TasksFactory.makeTask());
        Task task2 = manager.createTask(TasksFactory.makeTask());
        // Epic 1 -> Sub 1
        EpicTask epic1 = manager.createTask(TasksFactory.makeEpic());
        SubTask sub1 = manager.createTask(TasksFactory.makeSub(epic1.getId()));
        // Epic 2 -> Sub 2
        EpicTask epic2 = manager.createTask(TasksFactory.makeEpic());
        SubTask sub2 = manager.createTask(TasksFactory.makeSub(epic2.getId()));

        // Build history
        manager.getEpicTask(epic2.getId());
        manager.getEpicTask(epic1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId());
        manager.getSubTask(sub2.getId());
        manager.getSubTask(sub1.getId());
        assertEquals(
                getTaskIds(List.of(
                        epic2,
                        epic1,
                        task2,
                        task1,
                        sub2,
                        sub1
                )),
                getTaskIds(manager.getHistory())
        );

        // Change history order
        // "После каждого запроса выведите историю и убедитесь, что в ней нет повторов."
        manager.getEpicTask(epic1.getId());
        manager.getTask(task1.getId());
        manager.getSubTask(sub1.getId());
        assertEquals(
                getTaskIds(List.of(
                        epic2,
                        task2,
                        sub2,
                        epic1,
                        task1,
                        sub1
                )),
                getTaskIds(manager.getHistory())
        );

        // Remove Regular Task
        manager.removeTask(task1.getId());
        assertEquals(
                getTaskIds(List.of(
                        epic2,
                        task2,
                        sub2,
                        epic1,
                        sub1
                )),
                getTaskIds(manager.getHistory())
        );

        // "Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться."
        // Remove Sub Task
        manager.removeTask(sub1.getId());
        assertEquals(
                getTaskIds(List.of(
                        epic2,
                        task2,
                        sub2,
                        epic1
                )),
                getTaskIds(manager.getHistory())
        );

        // "Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи."
        // Remove Epic with nested Sub
        manager.removeTask(epic2.getId());
        assertEquals(
                getTaskIds(List.of(
                        task2,
                        epic1
                )),
                getTaskIds(manager.getHistory())
        );

        // Remove all Tasks
        manager.createTask(TasksFactory.makeTask());
        manager.createTask(TasksFactory.makeSub(epic1.getId()));
        manager.removeEpicTasks();
        manager.removeTasks();
        assertEquals(
                getTaskIds(List.of()),
                getTaskIds(manager.getHistory())
        );
    }

    // <<< Changing by link

    /**
     * @see TaskManager#getPrioritizedTasks()
     */
    @Test
    public void shouldGetPrioritizedTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        tasks.add(manager.createTask(TasksFactory.makeTask()));
        tasks.add(manager.createTask(TasksFactory.makeTask()));
        manager.createTask(
                new Task(
                        0,
                        "Not prioritized Task",
                        "Task without start time"
                )
        );

        EpicTask epic1 = manager.createTask(TasksFactory.makeEpic());
        tasks.add(manager.createTask(TasksFactory.makeSub(epic1.getId())));
        tasks.add(manager.createTask(TasksFactory.makeSub(epic1.getId())));

        EpicTask epic2 = manager.createTask(TasksFactory.makeEpic());
        tasks.add(manager.createTask(TasksFactory.makeSub(epic2.getId())));
        tasks.add(manager.createTask(TasksFactory.makeSub(epic2.getId())));

        assertEquals(
                tasks.stream()
                        .sorted(Comparator.comparing(Task::getStartTime))
                        .map(Task::getId)
                        .collect(Collectors.toCollection(ArrayList::new)),
                (ArrayList<Integer>) manager.getPrioritizedTasks()
                        .stream()
                        .map(Task::getId)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @Test
    public void shouldNotAllowToAddOrUpdateIntersectingTasks() {
        int[][] intervals = {
                {1, 5},
                {101, 105}
        }; // Existed interval
        int[][] intersections = { // Intervals which intersect the existed one
                {3, 4},
                {0, 3},
                {2, 4},
                {0, 6},
        };
        int[][] notIntersections = { // Intervals which don't intersect the existed ones
                {-3, -1},
                {-1, 1},
                {5, 7},
                {7, 10},
        };

        List<Task> existed = Arrays
                .stream(intervals)
                .map(
                        interval -> manager.createTask(
                                TasksFactory.makeTask(
                                        interval[0],
                                        interval[1]
                                )
                        )
                )
                .toList();

        EpicTask epic = manager.createTask(TasksFactory.makeEpic());

        for (int[] intersection : intersections) {
            // Try to create intersecting Task
            assertThrows(
                    IllegalArgumentException.class,
                    () -> manager.createTask(
                            TasksFactory.makeTask(
                                    intersection[0],
                                    intersection[1]
                            )
                    )
            );
            // Try to create intersecting Sub Task
            assertThrows(
                    IllegalArgumentException.class,
                    () -> manager.createTask(
                            TasksFactory.makeSub(
                                    epic.getId(),
                                    intersection[0],
                                    intersection[1]
                            )
                    )
            );
            // Try to update Task making it intersecting
            assertThrows(
                    IllegalArgumentException.class,
                    () -> manager.updateTask(
                            TasksFactory.setTimeRange(
                                    existed.getLast(),
                                    intersection[0],
                                    intersection[1]
                            )
                    )
            );
        }

        for (int[] notIntersection : notIntersections) {
            // Create NOT intersecting Task
            manager.removeTask(
                    manager.createTask(
                                    TasksFactory.makeTask(
                                            notIntersection[0],
                                            notIntersection[1]
                                    )
                            )
                            .getId()
            );
            // Create NOT intersecting Sub Task
            manager.removeTask(
                    manager.createTask(
                                    TasksFactory.makeSub(
                                            epic.getId(),
                                            notIntersection[0],
                                            notIntersection[1]
                                    )
                            )
                            .getId()
            );
            // Try to update Task making it NOT intersecting
            manager.updateTask(
                    TasksFactory.setTimeRange(
                            existed.getLast(),
                            notIntersection[0],
                            notIntersection[1]
                    )
            );
        }
    }

    protected void assertTaskContent(Task task, int id, String name) {
        assertEquals(id, task.getId());
        assertEquals(name, task.getName());
    }

    protected void assertEqualsByContent(Task taskA, Task taskB) {
        assertEquals(taskA.getName(), taskB.getName());
        assertEquals(taskA.getStatus(), taskB.getStatus());
        assertEquals(taskA.getDescription(), taskB.getDescription());
        assertEquals(taskA.getStartTime().getSecond(), taskB.getStartTime().getSecond());
        assertEquals(taskA.getDuration().toMinutes(), taskB.getDuration().toMinutes());
    }

    protected List<Integer> getTaskIds(List<Task> tasks) {
        return tasks.stream().map(Task::getId).toList();
    }
}