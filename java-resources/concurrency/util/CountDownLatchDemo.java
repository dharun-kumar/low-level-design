import java.util.concurrent.CountDownLatch;

class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {

       CountDownLatch latch = new CountDownLatch(3);

        Runnable component = () -> {
            System.out.println(Thread.currentThread().getName() + " initialized");
            latch.countDown();
        };

        new Thread(component, "Database").start();
        new Thread(component, "Cache").start();
        new Thread(component, "MessageBroker").start();

        latch.await();
        System.out.println("All components ready. Application starting.");

    }
}