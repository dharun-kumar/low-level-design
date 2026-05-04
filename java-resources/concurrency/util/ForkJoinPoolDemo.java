import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import static java.util.stream.LongStream.rangeClosed;

public class ForkJoinPoolDemo {
    public static void main(String[] args) {
        try (ForkJoinPool pool = new ForkJoinPool()) {
            System.out.println(pool.invoke(new SimpleSum(1, 1000)));
        }
    }
}

class SimpleSum extends RecursiveTask<Long> {

    private final long start, end;
    private static final int THRESHOLD = 100;

    SimpleSum(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            return rangeClosed(start, end).sum();
        }
        
        long mid = start + (end - start) / 2;
        SimpleSum left = new SimpleSum(start, mid);
        SimpleSum right = new SimpleSum(mid + 1, end);

        left.fork(); // Run left in background
        return right.compute() + left.join(); // Run right here and merge
    }

}