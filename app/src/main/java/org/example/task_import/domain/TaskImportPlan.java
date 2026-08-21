package org.example.task_import.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Immutable result of a completed task import. */
public final class TaskImportPlan {
    private final List<ImportedTask> tasks;
    private final List<String> aggregateLabels;
    private final Map<UUID, ImportedTask> taskMap;

    /**
     * Creates an immutable plan and lookup index from already-imported tasks.
     *
     * @param tasks non-null tasks in final order; no task or task ID may be null or duplicated
     * @param aggregateLabels non-null aggregate labels in final encounter order; no element may be null
     * @throws NullPointerException if either collection, a task, or an aggregate-label element is null
     * @throws IllegalStateException if two tasks have the same ID and no unambiguous lookup index can be built
     */
    public TaskImportPlan(List<ImportedTask> tasks, List<String> aggregateLabels) {
        this.tasks = List.copyOf(tasks);
        this.aggregateLabels = List.copyOf(aggregateLabels);
        this.taskMap = tasks.stream()
                .collect(Collectors.toMap(
                        ImportedTask::id,
                        Function.identity()
                ));
    }

    /**
     * Returns tasks in their final import order.
     *
     * @return a reusable immutable task list
     */
    public List<ImportedTask> getTasks() {
        return tasks;
    }

    /**
     * Returns aggregate labels in final task and per-task encounter order.
     *
     * @return a reusable immutable label list
     */
    public List<String> getAggregateLabels() {
        return aggregateLabels;
    }

    /**
     * Looks up an imported task by ID without changing the plan.
     *
     * @param id the task ID to find; must not be {@code null}
     * @return the matching task, or {@link Optional#empty()} when the ID is absent
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public Optional<ImportedTask> findById(UUID id) {
        if (id == null) {
            throw new NullPointerException("id is null");
        }
        return Optional.ofNullable(taskMap.get(id));
    }
}
