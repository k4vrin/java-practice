package org.example.task_import.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable normalized task included in an import plan.
 *
 * @param id non-null task ID
 * @param title non-null, non-blank normalized title
 * @param priority preserved task priority
 * @param labels non-null immutable normalized labels
 */
public record ImportedTask(
        UUID id,
        String title,
        int priority,
        List<String> labels
) {

    /** Orders tasks by descending priority and then ascending UUID. */
    public static final Comparator<ImportedTask> COMPARATOR = Comparator.comparing(ImportedTask::priority)
            .reversed()
            .thenComparing(ImportedTask::id);

    /**
     * Creates an immutable imported task.
     *
     * @throws NullPointerException if {@code id}, {@code title}, {@code labels}, or any label element is null
     * @throws IllegalArgumentException if {@code title} is blank
     */
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

    /**
     * Returns the immutable normalized labels for this task in encounter order.
     *
     * @return a reusable immutable label list
     */
    @Override
    public List<String> labels() {
        return labels;
    }
}
