package models.history;

import models.tasks.Task;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> taskNodes = new HashMap<>();
    private final Node preHead;
    private final Node postTail;

    public InMemoryHistoryManager() {
        preHead = new Node(null, null);
        postTail = new Node(null, preHead);
        preHead.setNext(postTail);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return getNodes()
                .stream()
                .map(node -> node.getValue().copy()) // Return copies to avoid changing by link
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void add(Task task) {
        remove(task.getId());
        taskNodes.put(
                task.getId(),
                pushNode(new Node(task.copy()))
        );
    }

    @Override
    public void remove(int taskId) {
        Node node = taskNodes.remove(taskId);

        if (node != null) {
            removeNode(node);
        }
    }

    // Node methods >>>

    private Node pushNode(Node node) {
        Node prev = postTail.getPrev();
        node.setPrev(prev);
        node.setNext(postTail);
        prev.setNext(node);
        postTail.setPrev(node);

        return node;
    }

    private void removeNode(Node node) {
        Node prev = node.getPrev();
        Node next = node.getNext();

        node.setPrev(null);
        node.setNext(null);

        if (prev != null) {
            prev.setNext(next);
        }

        if (next != null) {
            next.setPrev(prev);
        }
    }

    private ArrayList<Node> getNodes() {
        Node node = preHead.getNext();
        ArrayList<Node> nodes = new ArrayList<>();

        while (node != postTail) {
            nodes.add(node);
            node = node.getNext();
        }

        return nodes;
    }

    // <<< Node methods

    private static class Node {
        private final Task value;
        private Node prev;
        private Node next;

        public Node(Task task, Node prev) {
            this.value = task;
            this.prev = prev;
        }

        public Node(Task task) {
            this.value = task;
        }

        public Task getValue() {
            return value;
        }

        public Node getPrev() {
            return prev;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }
}
