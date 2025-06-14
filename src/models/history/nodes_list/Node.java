package models.history.nodes_list;

public class Node<V> {
    private final V value;
    private Node<V> prev;
    private Node<V> next;

    public Node(V task, Node<V> prev, Node<V> next) {
        this.value = task;
        this.prev = prev;
        this.next = next;
    }

    public Node(V task, Node<V> prev) {
        this.value = task;
        this.prev = prev;
    }

    public Node(V task) {
        this.value = task;
    }

    public V getValue() {
        return value;
    }

    public Node<V> getPrev() {
        return prev;
    }

    public void setPrev(Node<V> prev) {
        this.prev = prev;
    }

    public Node<V> getNext() {
        return next;
    }

    public void setNext(Node<V> next) {
        this.next = next;
    }
}
