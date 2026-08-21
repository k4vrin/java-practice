package org.example.task_import.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaskImportDomainExceptionContractTest {

    @Test
    void planRejectsNullTaskCollection() {
        assertExactly(NullPointerException.class, () -> new TaskImportPlan(null, List.of()));
    }

    @Test
    void planRejectsNullTaskElement() {
        assertExactly(NullPointerException.class, () -> new TaskImportPlan(Collections.singletonList(null), List.of()));
    }

    @Test
    void planRejectsNullAggregateLabelCollection() {
        assertExactly(NullPointerException.class, () -> new TaskImportPlan(List.of(task(1)), null));
    }

    @Test
    void planRejectsNullAggregateLabelElement() {
        assertExactly(NullPointerException.class, () -> new TaskImportPlan(List.of(task(1)), Collections.singletonList(null)));
    }

    @Test
    void planRejectsDuplicateTaskIds() {
        assertExactly(IllegalStateException.class, () -> new TaskImportPlan(List.of(task(1), task(1)), List.of()));
    }

    @Test
    void importedTaskRejectsNullId() {
        assertExactly(NullPointerException.class, () -> new ImportedTask(null, "title", 0, List.of()));
    }

    @Test
    void importedTaskRejectsNullTitle() {
        assertExactly(NullPointerException.class, () -> new ImportedTask(id(1), null, 0, List.of()));
    }

    @Test
    void importedTaskRejectsBlankTitle() {
        assertExactly(IllegalArgumentException.class, () -> new ImportedTask(id(1), "  ", 0, List.of()));
    }

    @Test
    void importedTaskRejectsNullLabelCollection() {
        assertExactly(NullPointerException.class, () -> new ImportedTask(id(1), "title", 0, null));
    }

    @Test
    void importedTaskRejectsNullLabelElement() {
        assertExactly(NullPointerException.class, () -> new ImportedTask(id(1), "title", 0, Collections.singletonList(null)));
    }

    private static ImportedTask task(long value) {
        return new ImportedTask(id(value), "task-" + value, 0, List.of());
    }

    private static UUID id(long value) {
        return new UUID(0, value);
    }

    private static <T extends Throwable> void assertExactly(Class<T> expectedType, org.junit.jupiter.api.function.Executable executable) {
        T exception = assertThrows(expectedType, executable);

        assertEquals(expectedType, exception.getClass());
    }
}
