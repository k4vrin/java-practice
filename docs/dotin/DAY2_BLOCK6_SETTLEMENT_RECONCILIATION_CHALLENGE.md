# Day 2 Block 6 — Settlement Reconciliation Challenge

## Conditions

- Time: 60 minutes
- Internet documentation and search: allowed
- AI assistance and copied complete solutions: not allowed
- Do not open a reference solution during the attempt

## Scenario

A banking system has an internal ledger and receives a settlement file from an external processor. Implement the reconciliation operation so operations staff can see whether every reference agrees across the two sources.

The supplied domain records already validate their own scalar fields. Implement `SettlementReconciler.reconcile(...)` and add focused tests.

## Contract

Inputs:

- `ledgerEntries`: the internal ledger records
- `settlementEntries`: the external processor records

Rules:

1. Neither input list, nor any element inside either list, may be `null`.
2. A reference ID may occur at most once in each input. Any duplicate makes the entire call invalid and must throw `IllegalArgumentException`.
3. Match records by `referenceId`.
4. Produce exactly one result for every distinct reference appearing in either input.
5. Classify each result as:
   - `MATCHED`: both records exist and amount and currency are equal;
   - `DETAIL_MISMATCH`: both exist, but amount or currency differs;
   - `MISSING_SETTLEMENT`: only the ledger record exists;
   - `ORPHAN_SETTLEMENT`: only the settlement record exists.
6. Sort results by `referenceId` ascending, independent of input order.
7. `matchedCount` counts only `MATCHED` results.
8. `matchedAmountCents` sums the ledger amount of only `MATCHED` results.
9. Do not modify either input list.
10. The returned result list must be immutable.
11. Invalid input must not produce a partial report.

Use exact integer cents. Do not introduce floating-point money.

## Required evidence

Before editing, spend at most five minutes filling in `DAY2_BLOCK6_NOTES.md`:

- inputs and validation;
- result categories;
- chosen lookup state;
- predicted tests.

During the attempt:

- implement the simplest correct solution;
- keep the project compiling by minute 35;
- run the focused tests;
- add at least four useful tests beyond the supplied examples;
- reserve the final eight minutes for review and the written defense.

Run:

```bash
./gradlew :app:test --tests 'org.example.settlement_reconciliation.SettlementReconcilerTest'
git diff --check
```

## Defense

In the notes, briefly explain:

- your data structures and why you chose them;
- time and auxiliary-space complexity using `L` ledger rows and `S` settlement rows;
- which database constraint should prevent duplicate references;
- why an application `containsKey` check alone would not enforce uniqueness across concurrent database transactions.

## Scoring — 20 points

- Requirement extraction and validation: 4
- Reconciliation correctness: 6
- Adversarial tests: 4
- Data-structure/design judgment: 3
- Technical defense: 3

Target floor: **16/20**. A silent duplicate, omitted result category, mutable output, or non-running core is a critical correctness miss.

