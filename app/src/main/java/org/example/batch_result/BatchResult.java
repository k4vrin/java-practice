package org.example.batch_result;

import java.util.Objects;

public sealed interface BatchResult permits Success, Failure {

    static String describe(BatchResult result) {
        Objects.requireNonNull(result, "result cannot be null");

        return switch (result) {
            case Success s -> "OK: " + s.snapshot().labels().size() + " labels";
            case Failure f -> "ERROR: " + f.reason();
        };
    }
}

