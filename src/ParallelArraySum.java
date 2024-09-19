import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.Scanner;

public class ParallelArraySum {

    private static final int ARRAY_SIZE = 500000;

    public static void main(String[] args) {
        int[] array = generateArray(ARRAY_SIZE);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Input number of chunks:");
        int numberOfThreads = scanner.nextInt();

        if (numberOfThreads <= 0) {
            return;
        }

        try {
            long totalSum = parallelSum(array, numberOfThreads);
            System.out.println("Sum: " + totalSum);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public static int[] generateArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i + 1;
        }
        return array;
    }

    public static long parallelSum(int[] array, int numberOfThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<Long>> futures = new ArrayList<>();

        int chunkSize = array.length / numberOfThreads;
        int remainder = array.length % numberOfThreads;

        int start = 0;
        for (int i = 0; i < numberOfThreads; i++) {
            int end = start + chunkSize;

            if (i == numberOfThreads - 1) {
                end += remainder;
            }

            int[] chunk = new int[end - start];
            System.arraycopy(array, start, chunk, 0, chunk.length);

            futures.add(executor.submit(new ArraySumTask(chunk)));
            start = end;
        }

        long totalSum = 0;
        for (Future<Long> future : futures) {
            totalSum += future.get();
        }

        executor.shutdown();

        return totalSum;
    }

    public static class ArraySumTask implements Callable<Long> {
        private final int[] chunk;

        public ArraySumTask(int[] chunk) {
            this.chunk = chunk;
        }

        @Override
        public Long call() {
            long sum = 0;
            for (int value : chunk) {
                sum += value;
            }
            return sum;
        }
    }
}