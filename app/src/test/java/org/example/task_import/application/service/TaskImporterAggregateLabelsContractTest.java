package org.example.task_import.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.TaskImportPlan;
import org.example.task_import.domain.TaskRow;
import org.example.task_import.support.TaskImporterTestSupport;
import org.junit.jupiter.api.Test;

abstract class TaskImporterAggregateLabelsContractTest extends TaskImporterTestSupport {

    @Test
    void labelsFollowFinalSortedTaskOrder() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1, List.of("low-priority")),
                taskRow(2, 10, List.of("high-priority"))
        ));

        assertEquals(List.of("high-priority", "low-priority"), plan.getAggregateLabels());
    }

    @Test
    void labelsPreserveEachTasksInternalLabelOrder() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 10, List.of("first", "second", "third"))
        ));

        assertEquals(List.of("first", "second", "third"), plan.getAggregateLabels());
    }

    @Test
    void duplicateLabelsAcrossTasksAppearOnlyOnce() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1, List.of("backend")),
                taskRow(2, 10, List.of("backend"))
        ));

        assertEquals(List.of("backend"), plan.getAggregateLabels());
    }

    @Test
    void firstOccurrenceInFinalTaskOrderWins() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1, List.of("low-before", "shared")),
                taskRow(2, 10, List.of("shared", "high-after"))
        ));

        assertEquals(List.of("shared", "high-after", "low-before"), plan.getAggregateLabels());
    }

    @Test
    void tasksWithEmptyLabelsProduceAnEmptyAggregateLabelList() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1, List.of()),
                taskRow(2, 10, List.of())
        ));

        assertEquals(List.of(), plan.getAggregateLabels());
    }

    @Test
    void repeatedImportsProduceDeterministicAggregateLabels() {
        List<TaskRow> rows = List.of(
                taskRow(3, 20, List.of("third-task")),
                taskRow(2, 20, List.of("second-task", "shared")),
                taskRow(1, 20, List.of("first-task", "shared")),
                taskRow(4, -2, List.of("last-task"))
        );
        List<String> expectedLabels = List.of(
                "first-task", "shared", "second-task", "third-task", "last-task"
        );

        for (int execution = 0; execution < 5; execution++) {
            assertEquals(expectedLabels, importTasks(rows).getAggregateLabels());
        }
    }

    private TaskImportPlan importTasks(List<TaskRow> rows) {
        return newImporter().importTasks(sourceOf(rows));
    }

    private static TaskRow taskRow(long id, int priority, List<String> labels) {
        return new TaskRow(taskId(id), "Task " + id, priority, labels);
    }

    private static TaskRowSource sourceOf(List<TaskRow> rows) {
        return () -> rows;
    }

    private static UUID taskId(long value) {
        return new UUID(0, value);
    }
}
