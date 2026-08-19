package org.example.task_import;

import java.io.IOException;
import java.util.List;

public interface TaskRowSource {
    List<TaskRow> load() throws IOException;
}
