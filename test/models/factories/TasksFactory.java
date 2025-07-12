package models.factories;

import models.tasks.EpicTask;
import models.tasks.SubTask;
import models.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

public class TasksFactory {
    private static final Random rand = new Random();
    private static int lastEndHour = 0; // When the lastly default generated Task ends

    public static Task makeTask() {
        return new Task(
                makeId(),
                "Task" + makeSuffix(),
                makeDescription(),
                makeStartTime(),
                makeDuration()
        );
    }

    public static Task makeTask(int startInHours, int endInHours) {
        return setTimeRange(
                makeTask(),
                startInHours,
                endInHours
        );
    }

    public static Task makeTask(int id) {
        return new Task(
                id,
                "Task" + makeSuffix(),
                makeDescription(),
                makeStartTime(),
                makeDuration()
        );
    }

    public static EpicTask makeEpic() {
        return new EpicTask(
                makeId(),
                "Epic" + makeSuffix(),
                makeDescription()
        );
    }

    public static EpicTask makeEpic(int id) {
        return new EpicTask(
                id,
                "Epic" + makeSuffix(),
                makeDescription()
        );
    }

    public static SubTask makeSub(int epicId) {
        return new SubTask(
                makeId(),
                epicId,
                "Sub" + makeSuffix(),
                makeDescription(),
                makeStartTime(),
                makeDuration()
        );
    }

    public static SubTask makeSub(int epicId, int startInHours, int endInHours) {
        return setTimeRange(
                makeSub(epicId),
                startInHours,
                endInHours
        );
    }

    public static SubTask makeSub(int epicId, int id) {
        return new SubTask(
                id,
                epicId,
                "Sub" + makeSuffix(),
                makeDescription(),
                makeStartTime(),
                makeDuration()
        );
    }

    public static <T extends Task> T setTimeRange(T task, int startInHours, int endInHours) {
        LocalDateTime zeroTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        task.setStartTime(
                zeroTime.plusHours(startInHours)
        );
        task.setDuration(
                Duration.between(
                        task.getStartTime(),
                        zeroTime.plusHours(endInHours)
                )
        );
        return task;
    }

    private static int makeId() {
        return rand.nextInt();
    }

    private static LocalDateTime makeStartTime() {
        lastEndHour += 1;
        return LocalDateTime.now().plusHours(++lastEndHour);
    }

    private static Duration makeDuration() {
        lastEndHour += 1;
        return Duration.ofHours(1);
    }

    private static String makeDescription() {
        return "Description" + makeSuffix();
    }

    private static String makeSuffix() {
        return "-" + Math.abs(rand.nextInt());
    }
}
