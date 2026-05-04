import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

class CyclicBarrierDemo {

    public static void main(String[] args) {

        CyclicBarrier barrier = new CyclicBarrier(3, () ->
            System.out.println("All shards recovered. Moving to next index.")
        );

        Runnable worker = () -> {
            try {
                for (int index = 1; index <= 3; index++) {
                    System.out.println("recovered " + Thread.currentThread().getName() + " of index " + index);
                    barrier.await();
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
            }
        };

        new Thread(worker, "Shard-1").start();
        new Thread(worker, "Shard-2").start();
        new Thread(worker, "Shard-3").start();

    }
}