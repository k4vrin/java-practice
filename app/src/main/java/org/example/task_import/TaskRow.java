package org.example.task_import;

import java.util.List;
import java.util.UUID;

public record TaskRow(
        UUID id,
        String title,
        int priority,
        List<String> labels
) {
}