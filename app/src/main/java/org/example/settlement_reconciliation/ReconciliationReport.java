package org.example.settlement_reconciliation;

import java.util.List;

public record ReconciliationReport(
        List<ReconciliationResult> results,
        long matchedCount,
        long matchedAmountCents
) {
    public ReconciliationReport {
        if (results == null) {
            throw new IllegalArgumentException("results must not be null");
        }
        results = List.copyOf(results);
    }
}

