package org.example.task_registry;

import java.util.Comparator;
import java.util.UUID;

public interface Task {
    UUID id();
    int priority();

    Comparator<Task> COMPARATOR = Comparator.comparing(Task::priority)
            .reversed()
            .thenComparing(Task::id);

}
