# Dotin Day 1 Block 4 — Changed 20-Minute Recheck

## Conditions

- Total: 20 minutes.
- Internet/documentation allowed; no AI or copied solution.
- Work only in `org.example.inventory_reservation` and `BLOCK4_RECHECK_NOTES.md`.
- Do not weaken or delete supplied tests.

## Contract

`ReservationRequest` is valid only when `requestId` and `sku` are non-null and nonblank and `quantity > 0`. Invalid construction throws `IllegalArgumentException`.

`InventoryReservationService` starts from caller-supplied stock but owns an independent copy.

For `reserve(request)`:

1. Null request or unknown SKU throws `IllegalArgumentException` without changing stock or reservations.
2. Insufficient stock throws `IllegalStateException` without changing state.
3. The first valid request ID creates one reservation and decrements its SKU exactly once.
4. An exact replay returns the same reservation instance, changes nothing, and succeeds even if current stock is now below the requested quantity.
5. The same request ID with any different payload is a conflict: throw `IllegalArgumentException` before checking stock or mutating state.

`availableStock(sku)` returns current stock and rejects null, blank, or unknown SKU with `IllegalArgumentException`.

Single-threaded behavior is sufficient.

## Required work

1. Spend no more than four minutes filling `BLOCK4_RECHECK_NOTES.md` before editing code.
2. Run the focused suite.
3. Add at least three adversarial tests not already supplied, including null request, unknown SKU state preservation, and null SKU construction.
4. Make the smallest production repair.
5. Run focused tests and `git diff --check`.
6. Stop at 20 minutes and report remaining gaps honestly.

## Scoring — 10 points

- Requirements notes: 2.
- Adversarial tests: 2.
- Correct minimal repair and state preservation: 4.
- Complexity using named variables: 2.

Passing floor: 8/10 with no skipped written invariant or state-changing failure.

## Verification

```bash
./gradlew :app:test --tests 'org.example.inventory_reservation.InventoryReservationServiceTest'
git diff --check -- \
  app/src/main/java/org/example/inventory_reservation \
  app/src/test/java/org/example/inventory_reservation \
  docs/dotin/BLOCK4_RECHECK_NOTES.md
```
