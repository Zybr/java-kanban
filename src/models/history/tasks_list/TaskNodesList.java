package models.history.tasks_list;

import models.tasks.Task;

import java.util.ArrayList;

public class TaskNodesList {
    private final TaskNode preHead = new TaskNode(new Task(-1, "", ""));
    private final TaskNode postTail = new TaskNode(new Task(-2, "", ""), preHead);

    public TaskNodesList() {
        preHead.setNext(postTail);
    }

    public TaskNode addTask(Task task) {
        TaskNode prev = postTail.getPrev();
        TaskNode node = new TaskNode(task, prev, postTail);
        prev.setNext(node);
        postTail.setPrev(node);

        return node;
    }

    public void removeNode(TaskNode node) {
        TaskNode prev = node.getPrev();
        TaskNode next = node.getNext();

        node.setPrev(null);
        node.setNext(null);

        if (prev != null) {
            prev.setNext(next);
        }

        if (next != null) {
            next.setPrev(prev);
        }
    }

    public ArrayList<Task> getTasks() {
        TaskNode node = preHead.getNext();
        ArrayList<Task> tasks = new ArrayList<>();

        while (node != postTail) {
            tasks.add(node.getTask());
            node = node.getNext();
        }

        return tasks;
    }

    public ArrayList<Task> getReversedTask() {
        TaskNode node = postTail.getPrev();
        ArrayList<Task> tasks = new ArrayList<>();

        while (node != preHead) {
            tasks.add(node.getTask());
            node = node.getPrev();
        }

        return tasks;
    }
}
