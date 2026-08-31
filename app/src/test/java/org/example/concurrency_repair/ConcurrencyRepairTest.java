package org.example.concurrency_repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConcurrencyRepairTest {
    private static final int THREADS = 12;
    private static final int OPERATIONS_PER_THREAD = 10_000;

    @Test
    @Timeout(10)
    void preserves_every_request_increment_under_contention() throws Exception {
        RequestCounter counter = new RequestCounter();

        runConcurrently(counter::increment);

        assertEquals(THREADS * OPERATIONS_PER_THREAD, counter.value());
    }

    @Test
    @Timeout(10)
    void preserves_every_frequency_increment_for_the_same_key() throws Exception {
        FailureFrequency frequency = new FailureFrequency();

        runConcurrently(() -> frequency.record("TIMEOUT"));

        assertEquals(THREADS * OPERATIONS_PER_THREAD, frequency.count("TIMEOUT"));
    }

    @Test
    void snapshot_is_both_immutable_and_unaffected_by_later_calls_to_record() {
        FailureFrequency frequency = new FailureFrequency();

        frequency.record("TIMEOUT");
        Map<String, Integer> snapshot = frequency.snapshot();

        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.put("FRAUD", 1)
        );

        frequency.record("TIMEOUT");

        assertEquals(1, snapshot.get("TIMEOUT"));
        assertEquals(2, frequency.count("TIMEOUT"));
    }

    @Test
    void rejects_invalid_failure_codes_without_changing_state() {
        FailureFrequency frequency = new FailureFrequency();

        assertThrows(IllegalArgumentException.class, () -> frequency.record(" "));
        assertEquals(0, frequency.snapshot().size());
    }

    @Test
    @Timeout(10)
    void preserves_concurrent_updates_for_multiple_failure_codes() throws Exception {
        FailureFrequency frequency = new FailureFrequency();

        runConcurrently(() -> {
            frequency.record("TIMEOUT");
            frequency.record("DECLINED");
        });

        int expected = THREADS * OPERATIONS_PER_THREAD;
        assertEquals(expected, frequency.count("TIMEOUT"));
        assertEquals(expected, frequency.count("DECLINED"));
    }

    private static void runConcurrently(Runnable operation) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int thread = 0; thread < THREADS; thread++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    await(start);
                    for (int operationIndex = 0; operationIndex < OPERATIONS_PER_THREAD; operationIndex++) {
                        operation.run();
                    }
                }));
            }

            if (!ready.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("workers did not become ready");
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("worker interrupted", interrupted);
        }
    }
}
