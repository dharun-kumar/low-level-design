import java.util.concurrent.*;

public class CompletableFutureDemo {

    static ExecutorService executor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {

        // these two run in parallel
        CompletableFuture<Double> priceFuture = CompletableFuture.supplyAsync(() -> 499.99, executor);
        CompletableFuture<Boolean> stockFuture = CompletableFuture.supplyAsync(() -> true, executor);

        priceFuture
            .thenApply(price -> price + (price * 0.18))                                 // trasform
            .thenCombine(stockFuture, (price, inStock) -> {                             // merge independent future
                if (!inStock) throw new RuntimeException("Out of stock");
                return price;
            })
            .thenCompose(price -> CompletableFuture.supplyAsync(                        // chain dependent future
                () -> "Order placed price: " + price + " Shipping scheduled", executor
            ))
            .exceptionally(e -> "Order failed: " + e.getMessage())                      // handle error
            .thenAccept(System.out::println)                                            // consume result
            .join();                                                                    // wait for completion

        executor.shutdown();
    }
}