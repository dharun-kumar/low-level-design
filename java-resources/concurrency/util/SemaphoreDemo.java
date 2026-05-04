import java.util.concurrent.Semaphore;

class SemaphoreDemo {

    public static void main(String[] args) {

        Semaphore connectionPool = new Semaphore(2);

        Runnable task = () -> {
            try {
                connectionPool.acquire();
                System.out.println(Thread.currentThread().getName() + " acquired connection");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                connectionPool.release();
                System.out.println(Thread.currentThread().getName() + " released connection");
            }
        };

        for (int i = 0; i < 5; i++) {
            new Thread(task, "Thread-" + i).start();
        }
    }

}