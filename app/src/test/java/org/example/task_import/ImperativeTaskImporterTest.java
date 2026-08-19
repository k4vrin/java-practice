package org.example.task_import;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImperativeTaskImporterTest {

    @Test
    void rejectsNullRow() {
        TaskImportValidationException exception = assertValidationFailure(
                Collections.singletonList(null)
        );

        assertTrue(exception.getMessage().contains("field=row"));
    }

    @Test
    void rejectsNullTaskId() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(null, "Valid title", 0, List.of()))
        );

        assertTrue(exception.getMessage().contains("field=id"));
    }

    @Test
    void rejectsNullTitle() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(1), null, 0, List.of()))
        );

        assertTrue(exception.getMessage().contains("field=title"));
    }

    @Test
    void rejectsEmptyTitleAfterTrimming() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(2), "", 0, List.of()))
        );

        assertTrue(exception.getMessage().contains("field=title"));
    }

    @Test
    void rejectsWhitespaceOnlyTitleAfterTrimming() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(3), "   ", 0, List.of()))
        );

        assertTrue(exception.getMessage().contains("field=title"));
    }

    @Test
    void rejectsNullLabelCollection() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(4), "Valid title", 0, null))
        );

        assertTrue(exception.getMessage().contains("field=labels"));
    }

    @Test
    void rejectsNullFirstLabelElement() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(5), "Valid title", 0, Collections.singletonList(null)))
        );

        assertTrue(exception.getMessage().contains("field=label[0]"));
    }

    @Test
    void rejectsNullLabelElementAtNonZeroIndex() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(6), "Valid title", 0, Arrays.asList("first", null)))
        );

        assertTrue(exception.getMessage().contains("field=label[1]"));
    }

    @Test
    void rejectsDuplicateTaskIdsInSameBatch() {
        UUID duplicateId = taskId(7);

        TaskImportValidationException exception = assertValidationFailure(
                List.of(validTask(duplicateId), validTask(duplicateId))
        );

        assertTrue(exception.getMessage().contains("field=id"));
    }

    @Test
    void duplicateTaskIdDiagnosticIncludesDuplicatedUuid() {
        UUID duplicateId = taskId(8);

        TaskImportValidationException exception = assertValidationFailure(
                List.of(validTask(duplicateId), validTask(duplicateId))
        );

        assertTrue(exception.getMessage().contains(duplicateId.toString()));
    }

    @Test
    void rejectsImportWhenInvalidDataAppearsAfterValidRows() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(validTask(taskId(9)), new TaskRow(taskId(10), null, 0, List.of()))
        );

        assertTrue(exception.getMessage().contains("field=title"));
    }

    @Test
    void diagnosticIdentifiesTheFailingRowIndex() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(validTask(taskId(11)), new TaskRow(taskId(12), null, 0, List.of()))
        );

        assertTrue(exception.getMessage().contains("row=1"));
    }

    @Test
    void diagnosticIdentifiesTheRelevantField() {
        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(taskId(13), "Valid title", 0, null))
        );

        assertTrue(exception.getMessage().contains("field=labels"));
    }

    @Test
    void diagnosticIncludesKnownTaskId() {
        UUID id = taskId(14);

        TaskImportValidationException exception = assertValidationFailure(
                List.of(new TaskRow(id, "Valid title", 0, null))
        );

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void acceptsNegativeAndVeryLargePriorities() {
        assertDoesNotThrow(() -> new ImperativeTaskImporter().importTasks(sourceOf(List.of(
                new TaskRow(taskId(15), "Negative priority", -1, List.of()),
                new TaskRow(taskId(16), "Large priority", Integer.MAX_VALUE, List.of())
        ))));
    }

    private static TaskImportValidationException assertValidationFailure(List<TaskRow> rows) {
        TaskImportValidationException exception = assertThrows(
                TaskImportValidationException.class,
                () -> new ImperativeTaskImporter().importTasks(sourceOf(rows))
        );

        assertEquals(TaskImportValidationException.class, exception.getClass());
        return exception;
    }

    private static TaskRow validTask(UUID id) {
        return new TaskRow(id, "Valid title", 0, List.of("label"));
    }

    private static TaskRowSource sourceOf(List<TaskRow> rows) {
        return () -> rows;
    }

    private static UUID taskId(long value) {
        return new UUID(0, value);
    }
}
