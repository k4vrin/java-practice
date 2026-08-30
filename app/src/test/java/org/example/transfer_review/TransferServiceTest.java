package org.example.transfer_review;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
    void submitsNotValidTransferAsPendingThrows() {
        TransferService service = new TransferService();

        assertThrows(IllegalArgumentException.class, () -> service.submit(request("tx-1", 1_000, null)));
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> request("tx-1", 0, List.of())
        );
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
    void cannotChangeAnApprovedTransferToRejected() {
        TransferService service = new TransferService();
        service.submit(request("tx-1", 1_000, List.of()));
        Transfer approved = service.approve("tx-1");

        assertThrows(IllegalStateException.class, () -> service.reject("tx-1"));
        assertEquals(TransferStatus.APPROVED, approved.status());
    }

    private static TransferRequest request(String transferId, long amountCents, List<String> labels) {
        return new TransferRequest(
                transferId,
                "account-1",
                "account-2",
                amountCents,
                labels
        );
    }
}
