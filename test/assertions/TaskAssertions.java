package assertions;

import models.tasks.Task;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskAssertions {
    public static void assertEqualByContent(Task taskA, Task taskB) {
        assertEquals(taskA.getName(), taskB.getName());
        assertEquals(taskA.getStatus(), taskB.getStatus());
        assertEquals(taskA.getDescription(), taskB.getDescription());
        assertEquals(taskA.getStartTime().getSecond(), taskB.getStartTime().getSecond());
        assertEquals(taskA.getDuration().toMinutes(), taskB.getDuration().toMinutes());
    }

    public static <T extends Task> void assertListsEqualByContent(ArrayList<T> listA, ArrayList<T> listB) {
        Assertions.assertEquals(
                listA.size(),
                listB.size()
        );

        for (int i = 0; i < listA.size(); i++) {
            assertEqualByContent(
                    listA.get(i),
                    listB.get(i)
            );
        }
    }
}
