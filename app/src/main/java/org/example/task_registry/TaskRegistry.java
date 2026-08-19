package org.example.task_registry;

import java.util.*;
import java.util.stream.Collectors;

public class TaskRegistry<T extends Task> {

    private final Set<UUID> dispatchedIds = new HashSet<>();
    private final Set<UUID> readyIds = new HashSet<>();
    private final Set<UUID> scheduledIds = new HashSet<>();
    private final Map<UUID, T> tasks = new HashMap<>();
    private final Deque<T> readyQueue = new ArrayDeque<>();
    private final PriorityQueue<T> scheduledQueue = new PriorityQueue<>(Task.COMPARATOR);
    private final Set<UUID> completedIds = new HashSet<>();
    private final List<T> completionHistory = new ArrayList<>();


    void registerAll(Collection<? extends T> tasks) {
        Set<UUID> uuids = new HashSet<>();
        for (T task : tasks) {
            if (task == null) {
                throw new IllegalArgumentException("task cannot be null");
            }
            if (!uuids.add(task.id()) || this.tasks.containsKey(task.id())) {
                throw new IllegalArgumentException("Duplicate task id " + task.id());
            }
        }

        this.tasks.putAll(
                tasks.stream()
                        .collect(Collectors.toMap(
                                Task::id,
                                task -> task
                        ))
        );
    }

    boolean markReady(UUID id) {
        var task = tasks.get(id);
        if (task == null || isAlreadyInLifecycleState(id)) {
            return false;
        }
        readyQueue.addLast(task);
        boolean addedToReadyIds = readyIds.add(id);
        if (!addedToReadyIds) {
            readyQueue.removeLast();
            return false;
        }
        return true;
    }

    Optional<T> dispatchNext() {
        var task = readyQueue.poll();
        if (task != null) {
            dispatchedIds.add(task.id());
            readyIds.remove(task.id());
        } else {
            task = scheduledQueue.poll();
            if (task != null) {
                scheduledIds.remove(task.id());
                dispatchedIds.add(task.id());
            }
        }

        return Optional.ofNullable(task);
    }

    boolean schedule(UUID id) {
        var task = tasks.get(id);
        if (task == null || isAlreadyInLifecycleState(id)) {
            return false;
        }
        scheduledQueue.add(task);
        if (!scheduledIds.add(id)) {
            scheduledQueue.remove(task);
            return false;
        }
        return true;
    }

    private boolean isAlreadyInLifecycleState(UUID id) {
        return scheduledIds.contains(id) || dispatchedIds.contains(id) || readyIds.contains(id) || completedIds.contains(id);
    }

    void complete(UUID id) {
        if (!tasks.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Task with id %s not found", id));
        }
        if (completedIds.contains(id)) {
            throw new IllegalStateException("Task " + id + " is already completed");
        }
        if (!dispatchedIds.contains(id)) {
            throw new IllegalStateException("Task " + id + " is registered but not dispatched");
        }
        dispatchedIds.remove(id);
        completedIds.add(id);
        completionHistory.add(tasks.get(id));
    }

     void drainCompletedTo(Collection<? super T> target) {
        target.addAll(completionHistory);
        completionHistory.clear();
     }

}
