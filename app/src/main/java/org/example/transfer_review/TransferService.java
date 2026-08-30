package org.example.transfer_review;

import java.util.HashMap;
import java.util.Map;

public final class TransferService {
    private final Map<String, Transfer> transfers = new HashMap<>();

    public Transfer submit(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        var transfer = transfers.get(request.transferId());
        var newTransfer = new Transfer(request);
        if (transfer == null) {
            transfers.put(request.transferId(), newTransfer);
            return newTransfer;
        } else {
            System.out.println("Equal: " + newTransfer.equals(transfer));
            if (!newTransfer.equals(transfer)) {
                throw new IllegalArgumentException(" idempotency conflict");
            } else {
                return transfer;
            }
        }

    }

    public Transfer approve(String transferId) {
        Transfer transfer = transfers.get(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer can not be null");
        }
        if (!transfer.status().equals(TransferStatus.PENDING)) {
            throw new IllegalStateException("Only pending transfers can be approved");
        }
        transfer.approve();
        return transfer;
    }

    public Transfer reject(String transferId) {
        Transfer transfer = transfers.get(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer can not be null");
        }
        if (!transfer.status().equals(TransferStatus.PENDING)) {
            throw new IllegalStateException("Only pending transfers can be rejected");
        }
        transfer.reject();
        return transfer;
    }

    public int size() {
        return transfers.size();
    }
}
