import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumer<T> {

    private final Queue<T> queue;
    private final int capacity;

    public ProducerConsumer(int capacity) {
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }

    private void produce(T item) throws InterruptedException {
        synchronized(queue) {   //used queue object as monitor to lock
            while(queue.size() == capacity) {
                System.out.println("Queue is full, calling wait in Producer ...");
                queue.wait();
            }
            System.out.println("Produced item " + item + ", notifies Consumer ...");
            queue.add(item);
            queue.notifyAll();
        }
    }

    private T consume() throws InterruptedException {
        T item = null;
        synchronized(queue) {   //used queue object as monitor to lock
            while(queue.isEmpty()) {
                System.out.println("Queue is empty, calling wait in Consumer ...");
                queue.wait();
            }
            System.out.println("Consumed item " + queue.peek() + ", notifies Producer ...");
            item = queue.poll();
            queue.notifyAll();
        }
        return item;
    }

    public static void main(String[] args) {

        ProducerConsumer<Integer> pubCon = new ProducerConsumer<>(10);

        Runnable producer = () -> {
            int item = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    pubCon.produce(item++);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Producer was interrupted, shutting down...");
            }
        };

        Runnable consumer = () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    pubCon.consume();
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Consumer was interrupted, shutting down...");
            }
        };

        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();

    }

}