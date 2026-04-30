import service.LRUCacheService;

public class LRUCacheDemo {

    public static void main(String[] args) {

        LRUCacheService<Integer, String> lruCacheService = new LRUCacheService<>(3);

        lruCacheService.put(1, "Alice");
        lruCacheService.put(2, "Bob");
        lruCacheService.put(3, "David");

        System.out.println(lruCacheService.get(1));

        lruCacheService.put(4, "John");

        System.out.println(lruCacheService.get(2));
    }

}