package org.example.task_import;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TaskImportPlan {
    private final List<ImportedTask> tasks;
    private final List<String> aggregateLabels;
    private final Map<UUID, ImportedTask> taskMap;

    public TaskImportPlan(List<ImportedTask> tasks, List<String> aggregateLabels) {
        this.tasks = List.copyOf(tasks);
        this.aggregateLabels = List.copyOf(aggregateLabels);
        this.taskMap = tasks.stream()
                .collect(Collectors.toMap(
                        ImportedTask::id,
                        Function.identity()
                ));
    }

    public List<ImportedTask> getTasks() {
        return tasks;
    }

    public List<String> getAggregateLabels() {
        return aggregateLabels;
    }

    public Optional<ImportedTask> getTaskById(UUID id) {
        if (id == null) {
            throw new NullPointerException("id is null");
        }
        return Optional.ofNullable(taskMap.get(id));
    }
}
