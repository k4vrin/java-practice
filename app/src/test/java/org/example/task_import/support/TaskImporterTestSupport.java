package org.example.task_import.support;

import org.example.task_import.application.port.in.TaskImporter;

/**
 * Base fixture for implementation-neutral task-import contract suites.
 *
 * <p>Concrete test classes supply one importer implementation, so the same
 * contract assertions run independently against both services.</p>
 */
public abstract class TaskImporterTestSupport {
    protected abstract TaskImporter createImporter();

    protected final TaskImporter newImporter() {
        return createImporter();
    }
}
