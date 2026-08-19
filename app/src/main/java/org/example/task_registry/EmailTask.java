package org.example.task_registry;

import java.util.UUID;

public class EmailTask implements Task {
    private final UUID id;
    private final int priority;

    private EmailTask(int priority, UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        this.priority = priority;
        this.id = id;
    }

    public static EmailTask create(int priority) {
        return new EmailTask(priority, UUID.randomUUID());
    }

    public static EmailTask createWithId(int priority, UUID id) {
        return new EmailTask(priority, id);
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
