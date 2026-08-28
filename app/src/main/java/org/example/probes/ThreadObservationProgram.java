package org.example.probes;

import java.util.concurrent.CountDownLatch;

/** Keeps a named application thread blocked long enough for a jcmd Thread.print observation. */
public final class ThreadObservationProgram {

    private ThreadObservationProgram() {
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch workerStarted = new CountDownLatch(1);
        Thread worker = new Thread(() -> waitForRelease(workerStarted), "jcmd-observation-worker");
        worker.start();
        workerStarted.await();

        System.out.println("READY");
        Thread.sleep(60_000);
    }

    private static void waitForRelease(CountDownLatch workerStarted) {
        workerStarted.countDown();
        try {
            Thread.sleep(60_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
