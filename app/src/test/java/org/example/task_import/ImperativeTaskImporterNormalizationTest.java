package org.example.task_import;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImperativeTaskImporterNormalizationTest {

    @Test
    void trimsTitleWhitespace() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(1), "  Fix Login Bug  ", 0, List.of("backend")
        ));

        assertEquals("Fix Login Bug", task.title());
    }

    @Test
    void preservesNegativePriorityExactly() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(2), "Task", -10, List.of("backend")
        ));

        assertEquals(-10, task.priority());
    }

    @Test
    void preservesZeroPriorityExactly() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(3), "Task", 0, List.of("backend")
        ));

        assertEquals(0, task.priority());
    }

    @Test
    void preservesMaximumIntegerPriorityExactly() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(4), "Task", Integer.MAX_VALUE, List.of("backend")
        ));

        assertEquals(Integer.MAX_VALUE, task.priority());
    }

    @Test
    void trimsAndLowercasesLabels() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(5), "Task", 0, List.of("  Backend  ")
        ));

        assertEquals(List.of("backend"), task.labels());
    }

    @Test
    void lowercasesTurkishCapitalIUsingLocaleRoot() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(6), "Task", 0, List.of("\u0130")
        ));

        assertEquals(List.of("i\u0307"), task.labels());
    }

    @Test
    void discardsLabelsThatAreBlankAfterTrimming() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(7), "Task", 0, List.of("", "   ", " \t ", "kept")
        ));

        assertEquals(List.of("kept"), task.labels());
    }

    @Test
    void removesDuplicateLabelsWithinOneTask() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(8), "Task", 0, List.of("bug", "bug")
        ));

        assertEquals(List.of("bug"), task.labels());
    }

    @Test
    void comparesLabelsForDuplicatesAfterNormalization() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(9), "Task", 0, List.of(" Bug ", "BUG", "bug")
        ));

        assertEquals(List.of("bug"), task.labels());
    }

    @Test
    void keepsTheFirstOccurrenceOfEachNormalizedLabel() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(10), "Task", 0, List.of(" First ", "SECOND", "second", "first", "third")
        ));

        assertEquals(List.of("first", "second", "third"), task.labels());
    }

    @Test
    void preservesEncounterOrderForNonDuplicateNormalizedLabels() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(11), "Task", 0, List.of(" Zeta ", "Alpha", "Beta ")
        ));

        assertEquals(List.of("zeta", "alpha", "beta"), task.labels());
    }

    @Test
    void producesAnEmptyImmutableLabelListWhenAllLabelsAreBlank() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(12), "Task", 0, List.of("", "   ", " \t ")
        ));

        assertEquals(List.of(), task.labels());
        assertThrows(UnsupportedOperationException.class, () -> task.labels().add("new-label"));
    }

    @Test
    void leavesAlreadyNormalizedTitleAndLabelsUnchanged() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(13), "Fix Login Bug", 0, List.of("backend", "bug")
        ));

        assertEquals("Fix Login Bug", task.title());
        assertEquals(List.of("backend", "bug"), task.labels());
    }

    @Test
    void doesNotMutateTheInputLabelList() {
        List<String> originalLabels = new ArrayList<>(List.of(" Backend ", "BUG", "bug"));
        List<String> originalContents = List.copyOf(originalLabels);
        TaskRow row = new TaskRow(taskId(14), "  Task  ", 0, originalLabels);

        importSingle(row);

        assertEquals(originalContents, originalLabels);
    }

    @Test
    void producesAnImmutableLabelList() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(15), "Task", 0, List.of("backend")
        ));

        assertThrows(UnsupportedOperationException.class, () -> task.labels().add("new-label"));
    }

    @Test
    void importsExactlyOneTaskForOneValidRow() {
        TaskImportPlan plan = new ImperativeTaskImporter().importTasks(sourceOf(List.of(
                new TaskRow(taskId(16), "Task", 0, List.of("backend"))
        )));

        assertEquals(1, plan.getTasks().size());
    }

    @Test
    void preservesTheOriginalTaskUuid() {
        UUID id = taskId(17);

        ImportedTask task = importSingle(new TaskRow(id, "Task", 0, List.of("backend")));

        assertEquals(id, task.id());
    }

    @Test
    void appliesAllNormalizationRulesTogether() {
        ImportedTask task = importSingle(new TaskRow(
                taskId(18),
                "  Fix Login Bug  ",
                -10,
                List.of(" Backend ", "BUG", "   ", "backend", " Urgent ", "", "bug")
        ));

        assertEquals("Fix Login Bug", task.title());
        assertEquals(-10, task.priority());
        assertEquals(List.of("backend", "bug", "urgent"), task.labels());
    }

    private static ImportedTask importSingle(TaskRow row) {
        TaskImportPlan plan = new ImperativeTaskImporter().importTasks(sourceOf(List.of(row)));

        assertEquals(1, plan.getTasks().size());
        return plan.getTasks().get(0);
    }

    private static TaskRowSource sourceOf(List<TaskRow> rows) {
        return () -> rows;
    }

    private static UUID taskId(long value) {
        return new UUID(0, value);
    }
}
