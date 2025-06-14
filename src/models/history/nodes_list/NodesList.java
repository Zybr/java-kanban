package models.history.nodes_list;

import java.util.ArrayList;

/**
 * The class could be a part of History Manager according to the task recommendation,
 * but decomposition makes supporting and testing easier.
 */
public class NodesList<V> {
    private final Node<V> preHead;
    private final Node<V> postTail;

    public NodesList() {
        preHead = new Node<>(null, null);
        postTail = new Node<>(null, preHead);
        preHead.setNext(postTail);
    }

    public Node<V> push(Node<V> node) {
        Node<V> prev = postTail.getPrev();
        node.setPrev(prev);
        node.setNext(postTail);
        prev.setNext(node);
        postTail.setPrev(node);

        return node;
    }

    public void remove(Node<V> node) {
        Node<V> prev = node.getPrev();
        Node<V> next = node.getNext();

        node.setPrev(null);
        node.setNext(null);

        if (prev != null) {
            prev.setNext(next);
        }

        if (next != null) {
            next.setPrev(prev);
        }
    }

    public ArrayList<V> getValues() {
        Node<V> node = preHead.getNext();
        ArrayList<V> values = new ArrayList<>();

        while (node != postTail) {
            values.add(node.getValue());
            node = node.getNext();
        }

        return values;
    }

    /**
     * The method is used only for testing purpose and might be removed later.
     */
    public ArrayList<V> getReversedValues() {
        Node<V> node = postTail.getPrev();
        ArrayList<V> values = new ArrayList<>();

        while (node != preHead) {
            values.add(node.getValue());
            node = node.getPrev();
        }

        return values;
    }
}
