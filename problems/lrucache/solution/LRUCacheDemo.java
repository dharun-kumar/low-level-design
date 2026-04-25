package solution;

public class LRUCacheDemo {

    static void main() {

        LRUCache<Integer, String> lruCache = new LRUCache<>(3);

        lruCache.put(1, "Alice");
        lruCache.put(2, "Bob");
        lruCache.put(3, "David");

        System.out.println(lruCache.get(1));

        lruCache.put(4, "John");

        System.out.println(lruCache.get(2));
    }

}