package org.example.task_import.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.example.task_import.application.exception.TaskImportException;
import org.example.task_import.application.exception.TaskImportValidationException;
import org.example.task_import.application.port.in.TaskImporter;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.ImportedTask;
import org.example.task_import.domain.TaskImportPlan;
import org.example.task_import.domain.TaskRow;
import org.junit.jupiter.api.Test;

class TaskImporterEquivalenceTest {

    @Test
    void complexValidBatchProducesEquivalentResults() {
        TaskImportPlan imperativePlan = importRows(new ImperativeTaskImporter(), complexRows());
        TaskImportPlan streamPlan = importRows(new StreamTaskImporter(), complexRows());

        assertEquivalentPlans(imperativePlan, streamPlan);
    }

    @Test
    void emptyInputIsEquivalent() {
        TaskImportPlan imperativePlan = importRows(new ImperativeTaskImporter(), List.of());
        TaskImportPlan streamPlan = importRows(new StreamTaskImporter(), List.of());

        assertNotNull(imperativePlan);
        assertNotNull(streamPlan);
        assertTrue(imperativePlan.getTasks().isEmpty());
        assertTrue(streamPlan.getTasks().isEmpty());
        assertTrue(imperativePlan.getAggregateLabels().isEmpty());
        assertTrue(streamPlan.getAggregateLabels().isEmpty());
    }

    @Test
    void lookupBehaviorIsEquivalent() {
        TaskImportPlan imperativePlan = importRows(new ImperativeTaskImporter(), complexRows());
        TaskImportPlan streamPlan = importRows(new StreamTaskImporter(), complexRows());

        for (TaskRow row : complexRows()) {
            Optional<ImportedTask> imperativeResult = imperativePlan.findById(row.id());
            Optional<ImportedTask> streamResult = streamPlan.findById(row.id());

            assertTrue(imperativeResult.isPresent());
            assertTrue(streamResult.isPresent());
            assertEquals(imperativeResult, streamResult);
        }

        assertTrue(imperativePlan.findById(taskId(99)).isEmpty());
        assertTrue(streamPlan.findById(taskId(99)).isEmpty());
    }

    @Test
    void validationFailureContractIsEquivalent() {
        assertEquivalentValidationFailure(Collections.singletonList(null));
        assertEquivalentValidationFailure(List.of(new TaskRow(taskId(10), "   ", 0, List.of())));
        assertEquivalentValidationFailure(List.of(
                new TaskRow(taskId(11), "First", 0, List.of()),
                new TaskRow(taskId(11), "Second", 0, List.of())
        ));
        assertEquivalentValidationFailure(List.of(
                new TaskRow(taskId(12), "Task", 0, Collections.singletonList(null))
        ));
    }

    @Test
    void infrastructureFailureContractIsEquivalent() {
        IOException imperativeCause = new IOException("disk read failed");
        IOException streamCause = new IOException("disk read failed");

        TaskImportException imperativeException = assertThrows(
                TaskImportException.class,
                () -> new ImperativeTaskImporter().importTasks(failingSource(imperativeCause))
        );
        TaskImportException streamException = assertThrows(
                TaskImportException.class,
                () -> new StreamTaskImporter().importTasks(failingSource(streamCause))
        );

        assertEquals(TaskImportException.class, imperativeException.getClass());
        assertEquals(TaskImportException.class, streamException.getClass());
        assertSame(imperativeCause, imperativeException.getCause());
        assertSame(streamCause, streamException.getCause());
        assertTrue(imperativeException.getMessage().toLowerCase(Locale.ROOT).contains("loading task rows"));
        assertTrue(streamException.getMessage().toLowerCase(Locale.ROOT).contains("loading task rows"));
    }

    @Test
    void repeatedExecutionsRemainDeterministicallyEquivalent() {
        TaskImportPlan expectedPlan = importRows(new ImperativeTaskImporter(), complexRows());

        for (int execution = 0; execution < 5; execution++) {
            assertEquivalentPlans(expectedPlan, importRows(new ImperativeTaskImporter(), complexRows()));
            assertEquivalentPlans(expectedPlan, importRows(new StreamTaskImporter(), complexRows()));
        }
    }

    private static void assertEquivalentPlans(TaskImportPlan imperativePlan, TaskImportPlan streamPlan) {
        assertEquals(taskIds(imperativePlan), taskIds(streamPlan));
        assertEquals(taskTitles(imperativePlan), taskTitles(streamPlan));
        assertEquals(taskPriorities(imperativePlan), taskPriorities(streamPlan));
        assertEquals(taskLabels(imperativePlan), taskLabels(streamPlan));
        assertEquals(imperativePlan.getAggregateLabels(), streamPlan.getAggregateLabels());
    }

    private static void assertEquivalentValidationFailure(List<TaskRow> rows) {
        TaskImportValidationException imperativeException = assertThrows(
                TaskImportValidationException.class,
                () -> importRows(new ImperativeTaskImporter(), rows)
        );
        TaskImportValidationException streamException = assertThrows(
                TaskImportValidationException.class,
                () -> importRows(new StreamTaskImporter(), rows)
        );

        assertEquals(TaskImportValidationException.class, imperativeException.getClass());
        assertEquals(TaskImportValidationException.class, streamException.getClass());
        assertEquals(diagnosticValue(imperativeException, "row"), diagnosticValue(streamException, "row"));
        assertEquals(diagnosticValue(imperativeException, "taskId"), diagnosticValue(streamException, "taskId"));
        assertEquals(diagnosticValue(imperativeException, "field"), diagnosticValue(streamException, "field"));
        assertEquals(diagnosticValue(imperativeException, "reason"), diagnosticValue(streamException, "reason"));
    }

    private static String diagnosticValue(TaskImportValidationException exception, String key) {
        String message = exception.getMessage();
        String prefix = key + "=";
        int valueStart = message.indexOf(prefix);

        assertTrue(valueStart >= 0, () -> "Missing diagnostic field: " + key);
        valueStart += prefix.length();
        int valueEnd = message.indexOf(", ", valueStart);
        return valueEnd >= 0 ? message.substring(valueStart, valueEnd) : message.substring(valueStart);
    }

    private static TaskImportPlan importRows(TaskImporter importer, List<TaskRow> rows) {
        return importer.importTasks(sourceOf(rows));
    }

    private static List<TaskRow> complexRows() {
        return List.of(
                new TaskRow(taskId(3), "  Third Task  ", 10,
                        List.of(" Backend ", "BUG", "   ", "backend", "urgent")),
                new TaskRow(taskId(4), "  Negative Task  ", -5, List.of("Urgent", "audit")),
                new TaskRow(taskId(1), "  First Task  ", 10, List.of("BUG", "api", "")),
                new TaskRow(taskId(2), "  Second Task  ", 20, List.of(" backend ", "API", " "))
        );
    }

    private static TaskRowSource sourceOf(List<TaskRow> rows) {
        return () -> rows;
    }

    private static TaskRowSource failingSource(IOException exception) {
        return () -> {
            throw exception;
        };
    }

    private static List<UUID> taskIds(TaskImportPlan plan) {
        return plan.getTasks().stream().map(ImportedTask::id).toList();
    }

    private static List<String> taskTitles(TaskImportPlan plan) {
        return plan.getTasks().stream().map(ImportedTask::title).toList();
    }

    private static List<Integer> taskPriorities(TaskImportPlan plan) {
        return plan.getTasks().stream().map(ImportedTask::priority).toList();
    }

    private static List<List<String>> taskLabels(TaskImportPlan plan) {
        return plan.getTasks().stream().map(ImportedTask::labels).toList();
    }

    private static UUID taskId(long value) {
        return new UUID(0, value);
    }
}
