package org.example.settlement_reconciliation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettlementReconcilerTest {
    private final SettlementReconciler reconciler = new SettlementReconciler();

    @Test
    void classifiesEveryReferenceAndSortsResults() {
        List<LedgerEntry> ledger = List.of(
                ledger("ref-3", 300, "IRR"),
                ledger("ref-1", 100, "IRR"),
                ledger("ref-2", 200, "IRR")
        );
        List<SettlementEntry> settlements = List.of(
                settlement("ref-4", 400, "IRR"),
                settlement("ref-1", 100, "IRR"),
                settlement("ref-2", 250, "IRR")
        );

        ReconciliationReport report = reconciler.reconcile(ledger, settlements);

        assertEquals(
                List.of("ref-1", "ref-2", "ref-3", "ref-4"),
                report.results().stream().map(ReconciliationResult::referenceId).toList()
        );
        assertEquals(
                List.of(
                        ReconciliationStatus.MATCHED,
                        ReconciliationStatus.DETAIL_MISMATCH,
                        ReconciliationStatus.MISSING_SETTLEMENT,
                        ReconciliationStatus.ORPHAN_SETTLEMENT
                ),
                report.results().stream().map(ReconciliationResult::status).toList()
        );
    }

    @Test
    void totalsOnlyExactMatches() {
        ReconciliationReport report = reconciler.reconcile(
                List.of(ledger("ref-1", 100, "IRR"), ledger("ref-2", 200, "IRR")),
                List.of(settlement("ref-1", 100, "IRR"), settlement("ref-2", 200, "USD"))
        );

        assertEquals(1, report.matchedCount());
        assertEquals(100, report.matchedAmountCents());
    }

    @Test
    void duplicateLedgerReferenceRejectsTheWholeCall() {
        List<LedgerEntry> ledger = List.of(
                ledger("ref-1", 100, "IRR"),
                ledger("ref-1", 200, "IRR")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reconciler.reconcile(ledger, List.of())
        );
    }

    @Test
    void reportDoesNotExposeMutableResults() {
        ReconciliationReport report = reconciler.reconcile(
                List.of(ledger("ref-1", 100, "IRR")),
                List.of(settlement("ref-1", 100, "IRR"))
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> report.results().clear()
        );
    }

    private static LedgerEntry ledger(String referenceId, long amountCents, String currency) {
        return new LedgerEntry(referenceId, amountCents, currency);
    }

    private static SettlementEntry settlement(String referenceId, long amountCents, String currency) {
        return new SettlementEntry(referenceId, amountCents, currency);
    }
}

