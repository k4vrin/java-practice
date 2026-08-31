package org.example.concurrency_repair;

import java.util.concurrent.atomic.AtomicInteger;

public final class RequestCounter {
    private final AtomicInteger value = new AtomicInteger(0);

    public void increment() {
        value.incrementAndGet();
    }

    public int value() {
        return value.get();
    }
}
