package org.example.transfer_review;

import java.util.HashMap;
import java.util.Map;

public final class TransferService {
    private final Map<String, Transfer> transfers = new HashMap<>();

    public Transfer submit(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        Transfer existing = transfers.get(request.transferId());
        if (existing == null) {
            Transfer created = new Transfer(request);
            transfers.put(request.transferId(), created);
            return created;
        }

        if (!existing.request().equals(request)) {
            throw new IllegalArgumentException("transferId is already associated with a different request");
        }
        return existing;
    }

    public Transfer approve(String transferId) {
        Transfer transfer = requireExisting(transferId);
        if (transfer.status() != TransferStatus.PENDING) {
            throw new IllegalStateException("Only pending transfers can be approved");
        }
        transfer.approve();
        return transfer;
    }

    public Transfer reject(String transferId) {
        Transfer transfer = requireExisting(transferId);
        if (transfer.status() != TransferStatus.PENDING) {
            throw new IllegalStateException("Only pending transfers can be rejected");
        }
        transfer.reject();
        return transfer;
    }

    public int size() {
        return transfers.size();
    }

    private Transfer requireExisting(String transferId) {
        if (transferId == null || transferId.isBlank()) {
            throw new IllegalArgumentException("transferId must not be null or blank");
        }

        Transfer transfer = transfers.get(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("unknown transferId: " + transferId);
        }
        return transfer;
    }
}
