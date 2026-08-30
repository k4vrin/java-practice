package org.example.transfer_review;

import java.util.List;
import java.util.Objects;

public final class TransferRequest {
    private final String transferId;
    private final String sourceAccountId;
    private final String destinationAccountId;
    private final long amountCents;
    private final List<String> labels;

    public TransferRequest(
            String transferId,
            String sourceAccountId,
            String destinationAccountId,
            long amountCents,
            List<String> labels
    ) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be greater than zero");
        }
        if (labels == null || labels.stream().anyMatch(label -> label == null || label.isBlank())) {
            throw new IllegalArgumentException("labels must not be null or contain blank values");
        }
        if (transferId == null || transferId.isBlank()) {
            throw new IllegalArgumentException("transferId must not be null or blank");
        }
        if (sourceAccountId == null || sourceAccountId.isBlank()) {
            throw new IllegalArgumentException("sourceAccountId must not be null or blank");
        }
        if (destinationAccountId == null || destinationAccountId.isBlank()) {
            throw new IllegalArgumentException("destinationAccountId must not be null or blank");
        }
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("source and destination accounts must differ");
        }

        this.transferId = transferId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amountCents = amountCents;
        this.labels = List.copyOf(labels);
    }

    public String transferId() {
        return transferId;
    }

    public String sourceAccountId() {
        return sourceAccountId;
    }

    public String destinationAccountId() {
        return destinationAccountId;
    }

    public long amountCents() {
        return amountCents;
    }

    public List<String> labels() {
        return labels;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TransferRequest that)) {
            return false;
        }
        return amountCents == that.amountCents
                && transferId.equals(that.transferId)
                && Objects.equals(sourceAccountId, that.sourceAccountId)
                && Objects.equals(destinationAccountId, that.destinationAccountId)
                && Objects.equals(labels, that.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, sourceAccountId, destinationAccountId, amountCents, labels);
    }
}
