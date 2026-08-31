package org.example.settlement_reconciliation;

public record SettlementEntry(String referenceId, long amountCents, String currency) {
    public SettlementEntry {
        validateReference(referenceId);
        validateAmount(amountCents);
        validateCurrency(currency);
    }

    private static void validateReference(String referenceId) {
        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException("referenceId must not be null or blank");
        }
    }

    private static void validateAmount(long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be greater than zero");
        }
    }

    private static void validateCurrency(String currency) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("currency must contain exactly three uppercase letters");
        }
    }
}

