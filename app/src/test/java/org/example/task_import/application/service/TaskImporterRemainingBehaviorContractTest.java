package org.example.task_import.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.example.task_import.application.exception.TaskImportException;
import org.example.task_import.application.exception.TaskImportValidationException;
import org.example.task_import.application.port.outbound.TaskRowSource;
import org.example.task_import.domain.ImportedTask;
import org.example.task_import.domain.TaskImportPlan;
import org.example.task_import.domain.TaskRow;
import org.example.task_import.support.TaskImporterTestSupport;
import org.junit.jupiter.api.Test;

abstract class TaskImporterRemainingBehaviorContractTest extends TaskImporterTestSupport {

    @Test
    void nullSourceThrowsExactlyNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> newImporter().importTasks(null));

        assertEquals(NullPointerException.class, exception.getClass());
    }

    @Test
    void nullRowCollectionReturnedBySourceThrowsExactlyNullPointerException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> newImporter().importTasks(() -> null));

        assertEquals(NullPointerException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("TaskRowSource.load"));
    }

    @Test
    void emptyInputReturnsANonNullPlan() {
        TaskImportPlan plan = importTasks(List.of());

        assertNotNull(plan);
    }

    @Test
    void emptyInputReturnsAnEmptyAggregateLabelCollection() {
        TaskImportPlan plan = importTasks(List.of());

        assertNotNull(plan.getAggregateLabels());
        assertTrue(plan.getAggregateLabels().isEmpty());
    }

    @Test
    void emptyPlanTaskCollectionIsImmutable() {
        TaskImportPlan plan = importTasks(List.of());

        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.getTasks().add(importedTask(1, "task", List.of()))
        );
    }

    @Test
    void emptyPlanAggregateLabelCollectionIsImmutable() {
        TaskImportPlan plan = importTasks(List.of());

        assertThrows(UnsupportedOperationException.class, () -> plan.getAggregateLabels().add("label"));
    }

    @Test
    void existingTaskIdReturnsAPresentOptional() {
        UUID id = taskId(1);
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        Optional<ImportedTask> result = plan.findById(id);

        assertNotNull(result);
        assertTrue(result.isPresent());
    }

    @Test
    void existingTaskIdReturnsTheExactImportedTask() {
        UUID id = taskId(2);
        TaskImportPlan plan = importTasks(List.of(taskRow(2, 0, List.of())));
        ImportedTask expected = plan.getTasks().get(0);
        Optional<ImportedTask> result = plan.findById(id);

        assertTrue(result.isPresent());
        assertSame(expected, result.orElseThrow());
    }

    @Test
    void missingValidUuidReturnsOptionalEmpty() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        assertTrue(plan.findById(taskId(2)).isEmpty());
    }

    @Test
    void emptyPlanLookupReturnsOptionalEmpty() {
        TaskImportPlan plan = importTasks(List.of());

        assertTrue(plan.findById(taskId(1)).isEmpty());
    }

    @Test
    void lookupNeverReturnsANullOptional() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        assertNotNull(plan.findById(taskId(2)));
    }

    @Test
    void nullLookupIdThrowsExactlyNullPointerException() {
        TaskImportPlan plan = importTasks(List.of());

        NullPointerException exception = assertThrows(NullPointerException.class, () -> plan.findById(null));

        assertEquals(NullPointerException.class, exception.getClass());
    }

    @Test
    void lookupDoesNotAlterPlanContentsOrTaskOrder() {
        TaskImportPlan plan = importTasks(List.of(
                taskRow(1, 1, List.of("low")),
                taskRow(2, 10, List.of("high"))
        ));
        List<UUID> expectedTaskIds = taskIds(plan);
        List<String> expectedLabels = plan.getAggregateLabels();

        plan.findById(taskId(1)).orElseThrow();

        assertEquals(expectedTaskIds, taskIds(plan));
        assertEquals(expectedLabels, plan.getAggregateLabels());
    }

    @Test
    void taskImportPlanDefensivelyCopiesItsConstructorTaskList() {
        List<ImportedTask> originalTasks = new ArrayList<>(List.of(importedTask(1, "task", List.of())));
        TaskImportPlan plan = new TaskImportPlan(originalTasks, List.of());

        originalTasks.clear();

        assertEquals(List.of(taskId(1)), taskIds(plan));
    }

    @Test
    void taskImportPlanDefensivelyCopiesItsConstructorAggregateLabelList() {
        List<String> originalLabels = new ArrayList<>(List.of("backend"));
        TaskImportPlan plan = new TaskImportPlan(List.of(importedTask(1, "task", List.of())), originalLabels);

        originalLabels.clear();

        assertEquals(List.of("backend"), plan.getAggregateLabels());
    }

    @Test
    void returnedTaskListCannotBeMutated() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.getTasks().add(importedTask(2, "other", List.of()))
        );
    }

    @Test
    void returnedAggregateLabelListCannotBeMutated() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of("backend"))));

        assertThrows(UnsupportedOperationException.class, () -> plan.getAggregateLabels().add("api"));
    }

    @Test
    void mutatingOriginalTaskRowLabelsAfterImportDoesNotChangeImportedTask() {
        List<String> originalLabels = new ArrayList<>(List.of("backend", "api"));
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, originalLabels)));

        originalLabels.clear();
        originalLabels.add("changed");

        assertEquals(List.of("backend", "api"), plan.getTasks().get(0).labels());
    }

    @Test
    void mutatingOriginalSourceCollectionsAfterImportDoesNotChangeThePlan() {
        List<String> highLabels = new ArrayList<>(List.of("high"));
        List<String> lowLabels = new ArrayList<>(List.of("low"));
        List<TaskRow> rows = new ArrayList<>(List.of(
                taskRow(1, 1, lowLabels),
                taskRow(2, 10, highLabels)
        ));
        TaskImportPlan plan = importTasks(rows);

        rows.clear();
        highLabels.clear();
        lowLabels.clear();

        assertEquals(List.of(taskId(2), taskId(1)), taskIds(plan));
        assertEquals(List.of("high", "low"), plan.getAggregateLabels());
        assertTrue(plan.findById(taskId(2)).isPresent());
    }

    @Test
    void collectionGettersAreReusableAndStable() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of("backend"))));

        assertEquals(plan.getTasks(), plan.getTasks());
        assertEquals(plan.getAggregateLabels(), plan.getAggregateLabels());
        assertEquals(List.of(taskId(1)), taskIds(plan));
        assertEquals(List.of("backend"), plan.getAggregateLabels());
    }

    @Test
    void successfulImportNeverReturnsNull() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        assertNotNull(plan);
    }

    @Test
    void taskGetterReturnsAReusableCollection() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        assertInstanceOf(List.class, plan.getTasks());
    }

    @Test
    void aggregateLabelGetterReturnsAReusableCollection() {
        TaskImportPlan plan = importTasks(List.of(taskRow(1, 0, List.of())));

        assertInstanceOf(List.class, plan.getAggregateLabels());
    }

    @Test
    void IOExceptionIsTranslatedToExactlyTaskImportException() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertEquals(TaskImportException.class, exception.getClass());
    }

    @Test
    void translatedInfrastructureFailureIsNotAnIOException() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertFalse(IOException.class.isInstance(exception));
    }

    @Test
    void translatedInfrastructureFailureRetainsTheExactIOExceptionCause() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertSame(original, exception.getCause());
    }

    @Test
    void translatedInfrastructureFailureMessageIdentifiesLoadingTaskRows() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertTrue(exception.getMessage().toLowerCase(Locale.ROOT).contains("loading task rows"));
    }

    @Test
    void originalLowLevelMessageRemainsAvailableThroughTheCause() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertEquals("disk read failed", exception.getCause().getMessage());
    }

    @Test
    void IOExceptionIsNotSwallowed() {
        IOException original = new IOException("disk read failed");

        assertThrows(TaskImportException.class, () -> newImporter().importTasks(failingSource(original)));
    }

    @Test
    void sourceLoadingFailureDoesNotReturnAPartialPlan() {
        IOException original = new IOException("disk read failed");

        assertThrows(TaskImportException.class, () -> newImporter().importTasks(failingSource(original)));
    }

    @Test
    void invalidTaskDataThrowsValidationExceptionNotTaskImportException() {
        TaskImportValidationException exception = assertThrows(
                TaskImportValidationException.class,
                () -> importTasks(List.of(new TaskRow(null, "Task", 0, List.of())))
        );

        assertEquals(TaskImportValidationException.class, exception.getClass());
        assertFalse(TaskImportException.class.isInstance(exception));
    }

    @Test
    void sourceIOExceptionThrowsTaskImportExceptionNotValidationException() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertFalse(TaskImportValidationException.class.isInstance(exception));
    }

    @Test
    void uncheckedSourceFailurePropagatesWithoutTranslation() {
        IllegalStateException original = new IllegalStateException("source is closed");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> newImporter().importTasks(() -> {
                    throw original;
                })
        );

        assertEquals(IllegalStateException.class, exception.getClass());
        assertSame(original, exception);
    }

    @Test
    void infrastructureMessageIsNotOnlyTheRawIOExceptionMessage() {
        IOException original = new IOException("disk read failed");

        TaskImportException exception = assertTaskImportFailure(original);

        assertNotEquals(original.getMessage(), exception.getMessage());
    }

    @Test
    void validationAndInfrastructureFailuresRemainDistinguishableByType() {
        TaskImportValidationException validationException = assertThrows(
                TaskImportValidationException.class,
                () -> importTasks(List.of(new TaskRow(null, "Task", 0, List.of())))
        );
        TaskImportException infrastructureException = assertTaskImportFailure(
                new IOException("disk read failed")
        );

        assertNotEquals(validationException.getClass(), infrastructureException.getClass());
    }

    private TaskImportException assertTaskImportFailure(IOException original) {
        TaskImportException exception = assertThrows(
                TaskImportException.class,
                () -> newImporter().importTasks(failingSource(original))
        );

        assertEquals(TaskImportException.class, exception.getClass());
        return exception;
    }

    private TaskImportPlan importTasks(List<TaskRow> rows) {
        return newImporter().importTasks(sourceOf(rows));
    }

    private static TaskRow taskRow(long id, int priority, List<String> labels) {
        return new TaskRow(taskId(id), "Task " + id, priority, labels);
    }

    private static ImportedTask importedTask(long id, String title, List<String> labels) {
        return new ImportedTask(taskId(id), title, 0, labels);
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

    private static UUID taskId(long value) {
        return new UUID(0, value);
    }
}
