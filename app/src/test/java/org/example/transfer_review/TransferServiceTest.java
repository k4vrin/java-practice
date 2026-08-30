package org.example.transfer_review;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferServiceTest {
    @Test
    void submitsValidTransferAsPending() {
        TransferService service = new TransferService();
        Transfer transfer = service.submit(request("tx-1", 1_000, List.of("salary")));

        assertEquals(TransferStatus.PENDING, transfer.status());
        assertEquals(1, service.size());
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> request("tx-1", 0, List.of()));
    }

    @Test
    void requestKeepsAnImmutableSnapshotOfLabels() {
        List<String> labels = new ArrayList<>(List.of("salary"));
        TransferRequest request = request("tx-1", 1_000, labels);
        labels.add("changed-later");

        assertEquals(List.of("salary"), request.labels());
        assertThrows(UnsupportedOperationException.class, () -> request.labels().add("external"));
    }

    @Test
    void equivalentDuplicateSubmissionIsIdempotent() {
        TransferService service = new TransferService();
        Transfer first = service.submit(request(new String("tx-1"), 1_000, List.of("salary")));
        Transfer duplicate = service.submit(request(new String("tx-1"), 1_000, List.of("salary")));

        assertSame(first, duplicate);
        assertEquals(1, service.size());
    }

    @Test
    void rejectsSameSourceAndDestination() {
        assertThrows(
                IllegalArgumentException.class,
                () -> fullRequest("tx-1", "account-1", "account-1", 1_000, List.of("salary"))
        );
    }

    @Test
    void rejectsNullOrBlankRequiredIds() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> fullRequest(null, "a", "b", 1_000, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> fullRequest(" ", "a", "b", 1_000, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> fullRequest("tx-1", null, "b", 1_000, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> fullRequest("tx-1", " ", "b", 1_000, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> fullRequest("tx-1", "a", null, 1_000, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> fullRequest("tx-1", "a", " ", 1_000, List.of()))
        );
    }

    @Test
    void rejectsNullOrInvalidLabels() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> request("tx-1", 1_000, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> request("tx-1", 1_000, List.of(""))),
                () -> assertThrows(IllegalArgumentException.class, () -> request("tx-1", 1_000, List.of(" "))),
                () -> assertThrows(IllegalArgumentException.class, () -> request("tx-1", 1_000, Arrays.asList("salary", null)))
        );
    }

    @Test
    void equivalentRequestsHaveEqualHashCodes() {
        TransferRequest first = request(new String("tx-1"), 1_000, List.of("salary"));
        TransferRequest second = request(new String("tx-1"), 1_000, List.of("salary"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void conflictingReplayPreservesOriginalTransferAndSize() {
        TransferService service = new TransferService();
        Transfer original = service.submit(request("tx-1", 1_000, List.of("salary")));
        service.approve("tx-1");

        assertThrows(IllegalArgumentException.class, () -> service.submit(request("tx-1", 2_000, List.of("salary"))));
        assertEquals(1, service.size());
        assertEquals(TransferStatus.APPROVED, original.status());
    }

    @Test
    void rejectsNullRequestSubmissionWithoutChangingSize() {
        TransferService service = new TransferService();

        assertThrows(IllegalArgumentException.class, () -> service.submit(null));
        assertEquals(0, service.size());
    }

    @Test
    void rejectsInvalidOrUnknownTransitionIds() {
        TransferService service = new TransferService();

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.approve(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.reject(" ")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.approve("missing"))
        );
    }

    @Test
    void bothTerminalStatesRejectEveryLaterTransition() {
        TransferService service = new TransferService();
        Transfer approved = service.submit(request("approved", 1_000, List.of()));
        Transfer rejected = service.submit(request("rejected", 1_000, List.of()));
        service.approve("approved");
        service.reject("rejected");

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> service.approve("approved")),
                () -> assertThrows(IllegalStateException.class, () -> service.reject("approved")),
                () -> assertThrows(IllegalStateException.class, () -> service.approve("rejected")),
                () -> assertThrows(IllegalStateException.class, () -> service.reject("rejected"))
        );
        assertEquals(TransferStatus.APPROVED, approved.status());
        assertEquals(TransferStatus.REJECTED, rejected.status());
    }

    private static TransferRequest request(String transferId, long amountCents, List<String> labels) {
        return fullRequest(transferId, "account-1", "account-2", amountCents, labels);
    }

    private static TransferRequest fullRequest(
            String transferId,
            String sourceAccountId,
            String destinationAccountId,
            long amountCents,
            List<String> labels
    ) {
        return new TransferRequest(transferId, sourceAccountId, destinationAccountId, amountCents, labels);
    }
}
