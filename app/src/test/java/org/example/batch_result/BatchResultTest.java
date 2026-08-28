package org.example.batch_result;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchResultTest {

    @Test
    void batchSnapshot_preservesContents() {
        BatchSnapshot snapshot = new BatchSnapshot(List.of("A", "B", "A", ""));

        assertEquals(List.of("A", "B", "A", ""), snapshot.labels());
    }

    @Test
    void batchSnapshot_rejectsNullList() {
        assertThrows(NullPointerException.class, () -> new BatchSnapshot(null));
    }

    @Test
    void batchSnapshot_rejectsNullElement() {
        assertThrows(
                NullPointerException.class,
                () -> new BatchSnapshot(java.util.Arrays.asList("A", null, "B"))
        );
    }

    @Test
    void batchSnapshot_defensivelyCopiesOriginalList() {
        List<String> labels = new ArrayList<>(List.of("A", "B"));
        BatchSnapshot snapshot = new BatchSnapshot(labels);

        labels.add("C");

        assertEquals(List.of("A", "B"), snapshot.labels());
    }

    @Test
    void batchSnapshot_accessorCannotMutateSnapshot() {
        BatchSnapshot snapshot = new BatchSnapshot(List.of("A", "B"));

        assertThrows(UnsupportedOperationException.class, () -> snapshot.labels().add("C"));
    }

    @Test
    void success_rejectsNullSnapshot() {
        assertThrows(NullPointerException.class, () -> new Success(null));
    }

    @Test
    void failure_rejectsNullReason() {
        assertThrows(NullPointerException.class, () -> new Failure(null));
    }

    @Test
    void failure_rejectsBlankReason() {
        assertThrows(IllegalArgumentException.class, () -> new Failure(""));
        assertThrows(IllegalArgumentException.class, () -> new Failure("   "));
        assertThrows(IllegalArgumentException.class, () -> new Failure("\t\n"));
    }

    @Test
    void failure_preservesValidReason() {
        Failure failure = new Failure("  database unavailable  ");

        assertEquals("  database unavailable  ", failure.reason());
    }

    @Test
    void describe_formatsSuccess() {
        BatchResult result = new Success(new BatchSnapshot(List.of("A", "B", "C")));

        assertEquals("OK: 3 labels", BatchResult.describe(result));
    }

    @Test
    void describe_formatsFailure() {
        BatchResult result = new Failure("database unavailable");

        assertEquals("ERROR: database unavailable", BatchResult.describe(result));
    }

    @Test
    void describe_rejectsNullResult() {
        assertThrows(NullPointerException.class, () -> BatchResult.describe(null));
    }

    @Test
    void success_canBeNarrowedWithInstanceofPatternMatching() {
        BatchResult result = new Success(new BatchSnapshot(List.of("A", "B")));

        assertTrue(result instanceof Success(BatchSnapshot snapshot) && snapshot.labels().size() == 2);
    }
}
