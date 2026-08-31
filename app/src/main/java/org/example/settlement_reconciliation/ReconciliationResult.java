package org.example.settlement_reconciliation;

public record ReconciliationResult(
        String referenceId,
        ReconciliationStatus status,
        LedgerEntry ledgerEntry,
        SettlementEntry settlementEntry
) {
}

