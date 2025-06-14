package models.history.tasks_list;

import models.tasks.Task;

public class TaskNode {
    private final Task task;
    private TaskNode prev;
    private TaskNode next;

    public TaskNode(Task task, TaskNode prev, TaskNode next) {
        this.task = task;
        this.prev = prev;
        this.next = next;
    }

    public TaskNode(Task task, TaskNode prev) {
        this.task = task;
        this.prev = prev;
    }

    public TaskNode(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public TaskNode getPrev() {
        return prev;
    }

    public void setPrev(TaskNode prev) {
        this.prev = prev;
    }

    public TaskNode getNext() {
        return next;
    }

    public void setNext(TaskNode next) {
        this.next = next;
    }
}
