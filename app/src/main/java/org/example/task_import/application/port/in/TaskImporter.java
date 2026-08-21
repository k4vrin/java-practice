package org.example.task_import.application.port.in;

import org.example.task_import.application.exception.TaskImportException;
import org.example.task_import.application.exception.TaskImportValidationException;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.TaskImportPlan;

/** Application input port for importing a source batch into an immutable task plan. */
public interface TaskImporter {
    /**
     * Loads, validates, normalizes, orders, and aggregates a source batch.
     *
     * @param source the row source to load; must not be {@code null}
     * @return a non-null immutable plan, including empty immutable collections for an empty batch
     * @throws NullPointerException if {@code source} is {@code null} or its {@link TaskRowSource#load()}
     *         method returns {@code null} instead of a row collection
     * @throws TaskImportException if loading task rows from {@code source} fails with an {@code IOException};
     *         the original {@code IOException} is retained as the cause
     * @throws TaskImportValidationException if the loaded batch contains a null row, null task ID, null or
     *         blank-after-trimming title, null label collection, null label element, or duplicate task ID
     */
    TaskImportPlan importTasks(TaskRowSource source);
}
