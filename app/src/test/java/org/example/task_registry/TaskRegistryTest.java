package org.example.task_registry;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskRegistryTest {

    @Test
    void registerAll_rejectsDuplicatedIdsWithinSameBatch() {
        UUID uuid = UUID.randomUUID();

        Task first = BugTask.createWithId(10, uuid);
        Task second = BugTask.createWithId(20, uuid);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerAll(List.of(first, second))
        );
    }

    // Reject an ID that was already registered in an earlier successful batch.
    @Test
    void registerAll_rejectsDuplicatedIdsAlreadyRegistered() {
        UUID uuid = UUID.randomUUID();

        Task first = BugTask.createWithId(10, uuid);
        Task duplicate = BugTask.createWithId(20, uuid);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerAll(List.of(duplicate))
        );
    }

    @Test
    void registerAll_failedBatchMakesNoPartialChanges() {
        UUID existingId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();

        Task first = BugTask.createWithId(10, existingId);
        Task second = BugTask.createWithId(20, newId);
        Task duplicate = BugTask.createWithId(20, existingId);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerAll(List.of(second, duplicate)));

        Assertions.assertDoesNotThrow(() -> registry.registerAll(List.of(second)));
    }

    @Test
    void registerAll_rejectsNullTaskAndMakesNoPartialChanges() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        Task first = BugTask.createWithId(10, firstId);
        Task second = BugTask.createWithId(20, secondId);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerAll(
                        java.util.Arrays.asList(first, null, second)
                )
        );

        Assertions.assertDoesNotThrow(
                () -> registry.registerAll(List.of(first, second))
        );
    }

    @Test
    void registerAll_registersAllTasksFromValidBatch() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        Task first = BugTask.createWithId(10, firstId);
        Task second = BugTask.createWithId(20, secondId);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        Assertions.assertDoesNotThrow(
                () -> registry.registerAll(List.of(first, second))
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerAll(
                        List.of(BugTask.createWithId(30, firstId))
                )
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.registerAll(
                        List.of(BugTask.createWithId(40, secondId))
                )
        );
    }

    @Test
    void markReady_registeredTaskIsDispatchedFromReadyQueue() {
        Task first = BugTask.create(10);
        Task second = BugTask.create(20);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first, second));

        registry.markReady(first.id());
        registry.markReady(second.id());

        Assertions.assertEquals(first, registry.dispatchNext().get());
    }

    @Test
    void markReady_unknownTaskIsRejected() {

        TaskRegistry<Task> registry = new TaskRegistry<>();

        boolean res = registry.markReady(UUID.randomUUID());

        Assertions.assertFalse(res);

    }

    @Test
    void markReady_sameTaskTwiceIsRejected() {
        Task first = BugTask.create(10);
        Task second = EmailTask.create(20);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first, second));
        registry.markReady(first.id());
        registry.markReady(second.id());

        Assertions.assertFalse(registry.markReady(first.id()));
    }

    @Test
    void markReady_dispatchedTaskCannotBecomeReadyAgain() {
        Task first = BugTask.create(10);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first));
        registry.markReady(first.id());
        registry.dispatchNext();
        Assertions.assertFalse(registry.markReady(first.id()));
    }

    @Test
    void schedule_registeredTaskCanBeScheduled() {
        Task first = BugTask.create(10);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first));

        Assertions.assertTrue(registry.schedule(first.id()));
    }

    @Test
    void schedule_unknownTaskIsRejected() {

        TaskRegistry<Task> registry = new TaskRegistry<>();

        boolean res = registry.schedule(UUID.randomUUID());

        Assertions.assertFalse(res);

    }

    @Test
    void schedule_sameTaskTwiceIsRejected() {
        Task first = BugTask.create(10);
        Task second = EmailTask.create(20);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first, second));
        registry.schedule(first.id());
        registry.schedule(second.id());

        Assertions.assertFalse(registry.schedule(first.id()));
    }

    @Test
    void schedule_readyTaskIsRejected() {
        Task first = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first));
        registry.markReady(first.id());
        Assertions.assertFalse(registry.schedule(first.id()));
    }

    @Test
    void markReady_scheduledTaskIsRejected() {
        Task first = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first));
        registry.schedule(first.id());
        Assertions.assertFalse(registry.markReady(first.id()));
    }

    @Test
    void schedule_dispatchedTaskCannotBecomeScheduledAgain() {
        Task first = BugTask.create(10);

        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(first));
        registry.schedule(first.id());
        registry.dispatchNext();
        Assertions.assertFalse(registry.schedule(first.id()));
    }

    @Test
    void dispatchNext_returnsHighestPriorityScheduledTask() {
        Task first = BugTask.create(10);
        Task second = BugTask.create(100);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first, second));
        registry.schedule(first.id());
        registry.schedule(second.id());
        Assertions.assertEquals(second, registry.dispatchNext().get());
    }

    @Test
    void dispatchNext_scheduledTasksAreDispatchedByDescendingPriority() {
        Task first = BugTask.create(10);
        Task second = BugTask.create(100);
        Task third = BugTask.create(50);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first, second, third));
        registry.schedule(first.id());
        registry.schedule(second.id());
        registry.schedule(third.id());
        Assertions.assertEquals(second, registry.dispatchNext().get());
        Assertions.assertEquals(third, registry.dispatchNext().get());
        Assertions.assertEquals(first, registry.dispatchNext().get());
    }

    @Test
    void dispatchNext_usesIdAsTieBreakerForEqualPriority() {
        UUID lowerId =
                UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID higherId =
                UUID.fromString("00000000-0000-0000-0000-000000000002");
        Task lowerIdTask = BugTask.createWithId(10, lowerId);
        Task higherIdTask = BugTask.createWithId(10, higherId);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(lowerIdTask, higherIdTask));
        registry.schedule(higherIdTask.id());
        registry.schedule(lowerIdTask.id());

        Assertions.assertEquals(lowerIdTask, registry.dispatchNext().get());
        Assertions.assertEquals(higherIdTask, registry.dispatchNext().get());
    }

    @Test
    void dispatchNext_prefersReadyTaskOverHigherPriorityScheduledTask() {
        Task first = BugTask.create(10);
        Task second = BugTask.create(100);
        Task third = BugTask.create(50);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first, second, third));
        registry.markReady(first.id());
        registry.schedule(second.id());
        registry.schedule(third.id());
        Assertions.assertEquals(first, registry.dispatchNext().get());
        Assertions.assertEquals(second, registry.dispatchNext().get());
        Assertions.assertEquals(third, registry.dispatchNext().get());
    }

    @Test
    void dispatchNext_readyTasksRemainFifo() {
        Task first = BugTask.create(10);
        Task second = BugTask.create(100);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first, second));
        registry.markReady(first.id());
        registry.markReady(second.id());
        Assertions.assertEquals(first, registry.dispatchNext().get());
        Assertions.assertEquals(second, registry.dispatchNext().get());
    }


    @Test
    void dispatchNext_fallsBackToScheduledQueueAfterReadyQueueIsEmpty() {
        Task first = BugTask.create(10);
        Task second = BugTask.create(2);
        Task third = BugTask.create(50);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first, second, third));
        registry.markReady(first.id());
        registry.markReady(second.id());
        registry.schedule(third.id());
        Assertions.assertEquals(first, registry.dispatchNext().get());
        Assertions.assertEquals(second, registry.dispatchNext().get());
        Assertions.assertEquals(third, registry.dispatchNext().get());
    }

    @Test
    void dispatchNext_marksScheduledTaskAsDispatched() {
        Task first = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();
        registry.registerAll(List.of(first));
        registry.markReady(first.id());
        registry.dispatchNext();
        Assertions.assertFalse(registry.markReady(first.id()));
    }

    @Test
    void dispatchNext_returnsEmptyWhenNoWorkExists() {
        TaskRegistry<Task> registry = new TaskRegistry<>();
        Assertions.assertTrue(registry.dispatchNext().isEmpty());
    }

    @Test
    void complete_dispatchedTaskCanBeCompleted() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());

        Assertions.assertEquals(task, registry.dispatchNext().get());

        Assertions.assertDoesNotThrow(
                () -> registry.complete(task.id())
        );
    }

    @Test
    void complete_registeredButNotDispatchedTaskIsRejected() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> registry.complete(task.id())
        );
    }

    @Test
    void complete_readyTaskIsRejected() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> registry.complete(task.id())
        );
    }

    @Test
    void complete_rejectedForReadyTaskLeavesItEligibleForNormalLifecycle() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> registry.complete(task.id())
        );

        Assertions.assertEquals(task, registry.dispatchNext().orElseThrow());
        Assertions.assertDoesNotThrow(() -> registry.complete(task.id()));
    }

    @Test
    void complete_scheduledTaskIsRejected() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.schedule(task.id());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> registry.complete(task.id())
        );
    }

    @Test
    void complete_unknownTaskIsRejected() {
        TaskRegistry<Task> registry = new TaskRegistry<>();

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.complete(UUID.randomUUID())
        );
    }

    @Test
    void complete_sameTaskTwiceIsRejected() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());
        registry.dispatchNext();

        registry.complete(task.id());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> registry.complete(task.id())
        );
    }

    @Test
    void drainCompletedTo_acceptsCollectionOfSupertype() {
        BugTask task = BugTask.create(10);

        TaskRegistry<BugTask> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());
        registry.dispatchNext();
        registry.complete(task.id());

        List<Object> target = new ArrayList<>();

        registry.drainCompletedTo(target);

        Assertions.assertEquals(List.of(task), target);
    }

    @Test
    void drainCompletedTo_appendsToExistingTargetContents() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());
        registry.dispatchNext();
        registry.complete(task.id());

        List<Task> target = new ArrayList<>();

        Task existing = EmailTask.create(50);
        target.add(existing);

        registry.drainCompletedTo(target);

        Assertions.assertEquals(
                List.of(existing, task),
                target
        );
    }

    @Test
    void markReady_completedTaskIsRejected() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.markReady(task.id());
        registry.dispatchNext();
        registry.complete(task.id());

        Assertions.assertFalse(registry.markReady(task.id()));
    }

    @Test
    void schedule_completedTaskIsRejected() {
        Task task = BugTask.create(10);
        TaskRegistry<Task> registry = new TaskRegistry<>();

        registry.registerAll(List.of(task));
        registry.schedule(task.id());
        registry.dispatchNext();
        registry.complete(task.id());

        Assertions.assertFalse(registry.schedule(task.id()));
    }
}
