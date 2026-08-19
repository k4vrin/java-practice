package org.example.task_import;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ImportedTask(
        UUID id,
        String title,
        int priority,
        List<String> labels
) {

    static Comparator<ImportedTask> COMPARATOR = Comparator.comparing(ImportedTask::priority)
            .reversed()
            .thenComparing(ImportedTask::id);

    public ImportedTask {
        Objects.requireNonNull(id, "id is null");
        Objects.requireNonNull(title, "title is null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title is empty");
        }
        Objects.requireNonNull(labels, "labels is null");
        for (String label : labels) {
            Objects.requireNonNull(label, "label is null");
        }
        labels = List.copyOf(labels);
    }
}
