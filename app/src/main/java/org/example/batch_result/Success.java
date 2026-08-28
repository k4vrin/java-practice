package org.example.batch_result;

import java.util.Objects;

public record Success(BatchSnapshot snapshot) implements BatchResult {
    public Success {
        Objects.requireNonNull(snapshot, "snapshot cannot be null");
    }
}
