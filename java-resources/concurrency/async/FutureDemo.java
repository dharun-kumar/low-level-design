import java.util.concurrent.*;

public class FutureDemo {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Double> calculatePrice = () -> {
            double price = 499.99;
            return price + (price * 0.18);
        };

        Callable<Boolean> verifyStock = () -> true;     // check inventory database

        Runnable shiporder = () -> System.out.println("Shipping scheduled for the order");

        try {
            Future<Double> priceFuture = executor.submit(calculatePrice);
            Future<Boolean> stockFuture = executor.submit(verifyStock);

            if (stockFuture.get() && priceFuture.get() > 0) {
                System.out.println("Order placed price: " + priceFuture.get());
                executor.submit(shiporder);
            }

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

}