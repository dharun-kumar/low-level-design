import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {

    private final ReentrantLock lock = new ReentrantLock(true); // Lock fairness; First-come, first-served
    private int count = 0;

    public void safeIncrement() {
        lock.lock(); // Explicitly acquire lock
        try {
            count++; // Critical section: only one thread enters
        } finally {
            lock.unlock(); // Always release in finally to prevent hung threads
        }
    }
    
}