package org.example.transfer_review;

import java.util.Objects;

public final class Transfer {
    private final TransferRequest request;
    private TransferStatus status;

    Transfer(TransferRequest request) {
        Objects.requireNonNull(request);
        this.request = request;
        this.status = TransferStatus.PENDING;
    }

    public TransferRequest request() {
        return request;
    }

    public TransferStatus status() {
        return status;
    }

    void approve() {
        status = TransferStatus.APPROVED;
    }

    void reject() {
        status = TransferStatus.REJECTED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Transfer that)) {
            return false;
        }
        return request.equals(that.request);
    }


    @Override
    public int hashCode() {
        return request.hashCode();
    }
}
