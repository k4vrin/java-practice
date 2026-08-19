package org.example.task_registry;

import java.util.UUID;

public class BugTask implements Task {
    private final UUID id;
    private final int priority;

    private BugTask(int priority, UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.priority = priority;
        this.id = id;
    }

    public static BugTask create(int priority) {
        return new BugTask(priority, UUID.randomUUID());
    }

    public static BugTask createWithId(int priority, UUID id) {
        return new BugTask(priority, id);
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public int priority() {
        return priority;
    }
}
