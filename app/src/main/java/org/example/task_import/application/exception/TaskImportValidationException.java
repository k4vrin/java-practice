package org.example.task_import.application.exception;

import java.util.UUID;

/** Signals malformed task data rejected during an import batch. */
public class TaskImportValidationException extends RuntimeException {

    /**
     * Creates a validation exception with safe row, field, and reason context.
     *
     * @param message the safe validation diagnostic
     */
    public TaskImportValidationException(String message) {
        super(message);
    }

    /**
     * Builds a safe validation diagnostic without including complete source-row contents.
     *
     * @param rowIndex the zero-based row index that failed
     * @param taskId the task ID when it was available before validation failed, otherwise {@code null}
     * @param field the rejected field name
     * @param reason the validation reason
     * @return the corresponding validation exception
     */
    public static TaskImportValidationException validationError(
            int rowIndex,
            UUID taskId,
            String field,
            String reason
    ) {
        return new TaskImportValidationException(
                "Task import validation failed: row=" + rowIndex
                        + ", taskId=" + taskId
                        + ", field=" + field
                        + ", reason=" + reason
        );
    }
}
