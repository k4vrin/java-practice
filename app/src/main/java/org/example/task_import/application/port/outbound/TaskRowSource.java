package org.example.task_import.application.port.outbound;

import java.io.IOException;
import java.util.List;
import org.example.task_import.domain.TaskRow;

/**
 * Outbound port for obtaining raw task rows from a source outside the import domain.
 *
 * <p>This domain-specific functional interface carries {@link IOException}, which
 * distinguishes source-reading failures from malformed task data. Standard function
 * interfaces cannot express that checked-failure boundary without changing the port's contract.</p>
 */
@FunctionalInterface
public interface TaskRowSource {
    /**
     * Loads the complete batch of raw task rows.
     *
     * @return a non-null row collection supplied by the underlying source; callers validate row contents separately
     * @throws IOException if the underlying source cannot be read or decoded into a row batch
     */
    List<TaskRow> load() throws IOException;
}
