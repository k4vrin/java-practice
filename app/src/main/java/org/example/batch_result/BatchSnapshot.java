package org.example.batch_result;

import java.util.List;

public record BatchSnapshot(List<String> labels) {

    public BatchSnapshot {
        labels = List.copyOf(labels); // Make an unmodifiable copy of the list
    }
}
