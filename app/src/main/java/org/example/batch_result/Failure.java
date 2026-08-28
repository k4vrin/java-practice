package org.example.batch_result;

import java.util.Objects;

public record Failure(String reason) implements BatchResult {
    public Failure {
        Objects.requireNonNull(reason, "reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
    }
}
