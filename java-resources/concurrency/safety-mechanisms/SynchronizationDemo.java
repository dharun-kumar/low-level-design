public class SynchronizationDemo {

    private static int globalCount = 0;

    private int instanceCount = 0;
    private final Object instanceLock = new Object();

    // Synchronization method with Class lock; Prevents simultaneous execution across ALL instances of this class.
    public static synchronized void incrementStaticMethod() {
        globalCount++;
    }

    // Synchronization block with Class lock; Explicit control
    public void incrementStaticBlock() {
        synchronized (SynchronizationDemo.class) {
            globalCount++;
        }
    }

    // Synchronization method with Object lock; Prevents multiple threads from accessing this method on the SAME instance.
    public synchronized void incrementMethod() {
        instanceCount++;
    }

    // Synchronization block with Object lock; Explicit control
    public void incrementBlock() {
        synchronized (instanceLock) {
            instanceCount++;
        }
    }

}