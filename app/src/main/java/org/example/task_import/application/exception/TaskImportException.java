package org.example.task_import.application.exception;

/** Signals an infrastructure failure encountered while importing task rows. */
public class TaskImportException extends RuntimeException {

    /**
     * Creates an import-level exception that preserves the low-level failure.
     *
     * @param message safe context about the import operation that failed
     * @param cause the underlying infrastructure failure; must not be discarded
     */
    public TaskImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
