package org.example.task_import.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.ImportedTask;
import org.example.task_import.domain.TaskImportPlan;
import org.example.task_import.domain.TaskRow;
import org.example.task_import.support.TaskImporterTestSupport;
import org.junit.jupiter.api.Test;

abstract class TaskImporterOrderingContractTest extends TaskImporterTestSupport {

    @Test
    void higherPriorityComesFirst() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1),
                taskRow(3, 5),
                taskRow(2, 10)
        ));

        assertEquals(List.of(taskId(2), taskId(3), taskId(1)), taskIds(plan));
    }

    @Test
    void equalPrioritiesUseAscendingUuid() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(3, 10),
                taskRow(1, 10),
                taskRow(2, 10)
        ));

        assertEquals(List.of(taskId(1), taskId(2), taskId(3)), taskIds(plan));
    }

    @Test
    void priorityTakesPrecedenceOverUuid() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1),
                taskRow(2, 10)
        ));

        assertEquals(List.of(taskId(2), taskId(1)), taskIds(plan));
    }

    @Test
    void negativePrioritiesAreOrderedCorrectly() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 5),
                taskRow(2, 0),
                taskRow(3, -10),
                taskRow(4, -2)
        ));

        assertEquals(List.of(5, 0, -2, -10), taskPriorities(plan));
    }

    @Test
    void maximumAndMinimumIntegerPrioritiesAreSupported() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, Integer.MIN_VALUE),
                taskRow(2, 0),
                taskRow(3, Integer.MAX_VALUE)
        ));

        assertEquals(List.of(Integer.MAX_VALUE, 0, Integer.MIN_VALUE), taskPriorities(plan));
    }

    @Test
    void inputOrderDoesNotOverridePriorityOrdering() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1),
                taskRow(2, 5),
                taskRow(3, 10)
        ));

        assertEquals(List.of(taskId(3), taskId(2), taskId(1)), taskIds(plan));
    }

    @Test
    void inputOrderDoesNotOverrideUuidTieBreaker() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(3, 10),
                taskRow(2, 10),
                taskRow(1, 10)
        ));

        assertEquals(List.of(taskId(1), taskId(2), taskId(3)), taskIds(plan));
    }

    @Test
    void singleTaskRemainsTheOnlyTask() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 10)));

        assertEquals(1, plan.getTasks().size());
        assertEquals(List.of(taskId(1)), taskIds(plan));
    }

    @Test
    void emptyInputProducesEmptyTaskList() {
        TaskImportPlan plan = importTasks(List.of());

        assertEquals(List.of(), taskIds(plan));
    }

    @Test
    void repeatedImportsProduceDeterministicOrder() {
        List<TaskRow> rows = List.of(
                taskRow(3, 10),
                taskRow(1, 10),
                taskRow(4, -5),
                taskRow(2, 5)
        );
        List<UUID> expectedOrder = List.of(taskId(1), taskId(3), taskId(2), taskId(4));

        for (int execution = 0; execution < 5; execution++) {
            assertEquals(expectedOrder, taskIds(importTasks(rows)));
        }
    }

    @Test
    void combinesPriorityAndUuidTieBreaking() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(3, 20),
                taskRow(1, 10),
                taskRow(2, 20),
                taskRow(4, 10),
                taskRow(5, -5)
        ));

        assertEquals(
                List.of(taskId(2), taskId(3), taskId(1), taskId(4), taskId(5)),
                taskIds(plan)
        );
    }

    private TaskImportPlan importTasks(List<TaskRow> rows) {
        return newImporter().importTasks(sourceOf(rows));
    }

    private static TaskRow taskRow(long id, int priority) {
        return new TaskRow(taskId(id), "Task " + id, priority, List.of("label"));
    }

    private static TaskRowSource sourceOf(List<TaskRow> rows) {
        return () -> rows;
    }

    private static List<UUID> taskIds(TaskImportPlan plan) {
        return plan.getTasks().stream().map(ImportedTask::id).toList();
    }

    private static List<Integer> taskPriorities(TaskImportPlan plan) {
        return plan.getTasks().stream().map(ImportedTask::priority).toList();
    }

    private static UUID taskId(long value) {
        return new UUID(0, value);
    }
}
