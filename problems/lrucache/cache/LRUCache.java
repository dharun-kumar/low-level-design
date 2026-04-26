package cache;

import dll.DoublyLinkedList;
import dll.Node;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K, V> {

    private final int CAPACITY;
    private final Map<K, Node<K, V>> CACHE;
    private final DoublyLinkedList<K,V> DOUBLY_LINKED_LIST;

    public LRUCache(int capacity) {
        CAPACITY= capacity;
        CACHE = new HashMap<>();
        DOUBLY_LINKED_LIST = new DoublyLinkedList<>();
    }

    public synchronized V get(K key) {
        if(!CACHE.containsKey(key)) {
            return null;
        }
        Node<K, V> node = CACHE.get(key);
        DOUBLY_LINKED_LIST.moveToFirst(node);
        return node.value;
    }

    public synchronized void put(K key, V value) {
        if(CACHE.containsKey(key)) {
            Node<K, V> node = CACHE.get(key);
            node.value = value;
            DOUBLY_LINKED_LIST.moveToFirst(node);
        } else {
            if(CACHE.size() == CAPACITY) {
                Node<K, V> node = DOUBLY_LINKED_LIST.removeLast();
                if(node != null) {
                    CACHE.remove(node.key);
                }
            }
            Node<K, V> node = new Node<>(key, value);
            CACHE.put(key, node);
            DOUBLY_LINKED_LIST.addFirst(node);
        }
    }

    public synchronized void remove(K key) {
        if(CACHE.containsKey(key)) {
            DOUBLY_LINKED_LIST.remove(CACHE.get(key));
            CACHE.remove(key);
        }
    }

}
