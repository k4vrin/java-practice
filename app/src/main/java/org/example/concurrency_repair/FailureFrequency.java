package org.example.concurrency_repair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FailureFrequency {
    private final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

    public void record(String failureCode) {
        if (failureCode == null || failureCode.isBlank()) {
            throw new IllegalArgumentException("failureCode must not be null or blank");
        }

        counts.compute(failureCode, (key, val)
                ->(val == null) ? 1 : val + 1);

    }

    public int count(String failureCode) {
        return counts.getOrDefault(failureCode, 0);
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(counts);
    }
}
