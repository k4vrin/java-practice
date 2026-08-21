package org.example.task_import.domain;

import java.util.List;
import java.util.UUID;

/**
 * Raw source data awaiting importer-level validation.
 *
 * <p>The canonical constructor deliberately accepts incomplete data so the importer can report the
 * source row index, task ID when available, field, and reason in one validation diagnostic.</p>
 *
 * @param id the source task ID, which may be null until importer validation
 * @param title the source title, which may be null or blank until importer validation
 * @param priority the source priority, including any integer value
 * @param labels the source labels, which may be null or contain null elements until importer validation
 */
public record TaskRow(
        UUID id,
        String title,
        int priority,
        List<String> labels
) {
    /** Creates an unvalidated raw task row. */
}
