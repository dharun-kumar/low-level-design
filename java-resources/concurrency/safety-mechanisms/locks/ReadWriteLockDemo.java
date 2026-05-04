import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockDemo {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private int sharedData = 0;

    public int read() {
        rwLock.readLock().lock(); // Multiple threads can hold this simultaneously
        try {
            return sharedData; 
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void write(int value) {
        rwLock.writeLock().lock(); // Blocks all readers and other writers
        try {
            this.sharedData = value;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
}