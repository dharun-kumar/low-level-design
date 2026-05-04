import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionDemo<T> {

    private final Queue<T> queue;
    private final int capacity;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canProduce = lock.newCondition();
    private final Condition canConsume = lock.newCondition();

    public ConditionDemo(int capacity) {
        this.queue = new LinkedList<>();
        this.capacity = capacity;
    }

    private void produce(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                canProduce.await();
            }
            queue.add(item);
            canConsume.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private T consume() throws InterruptedException {
        T item = null;
        lock.lock();
        try {
            while (queue.isEmpty()) {
                canConsume.await();
            }
            item = queue.poll();
            canProduce.signalAll();  
        } finally {
            lock.unlock();
        }
        return item;
    }

}