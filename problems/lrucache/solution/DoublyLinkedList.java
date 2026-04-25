package solution;

public class DoublyLinkedList<K, V> {

    private Node<K, V> head;
    private Node<K, V> tail;

    public DoublyLinkedList() {
        head = new Node<>(null, null);
        tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public void addFirst(Node<K,V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    public void remove(Node<K, V> node) {
      node.prev.next = node.next;
      node.next.prev = node.prev;
    }

    public void moveToFirst(Node<K, V> node) {
        remove(node);
        addFirst(node);
    }

    public Node<K, V> removeLast() {
        if(tail.prev != head) {
            Node<K,V> node = tail.prev;
            remove(node);
            return node;
        } return null;
    }

}
