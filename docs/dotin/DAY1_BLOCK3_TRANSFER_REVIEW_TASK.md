# Dotin Day 1 Block 3 — Transfer Review Debugging and Refactoring

## Assessment objective

Repair an unfamiliar in-memory transfer-review component so that it satisfies the business contract below. This block evaluates requirement extraction, root-cause diagnosis, minimal Java changes, edge-case testing, and technical explanation under practical assessment conditions.

This is not an algorithm exercise. Do not replace the supplied design with a framework, database, or large abstraction hierarchy.

## Time and tool rules

- Total time: 50 minutes.
- Internet and official documentation are allowed.
- IDE assistance, compiler output, Gradle, and local test reports are allowed.
- AI assistance and copied complete solutions are not allowed after the timer begins.
- Do not ask for diagnosis, code, or hints during the timed attempt.
- Work only in the supplied `org.example.transfer_review` production and test packages.
- Preserve unrelated repository changes.

## Starter files

Production:

- `app/src/main/java/org/example/transfer_review/TransferRequest.java`
- `app/src/main/java/org/example/transfer_review/Transfer.java`
- `app/src/main/java/org/example/transfer_review/TransferStatus.java`
- `app/src/main/java/org/example/transfer_review/TransferService.java`

Tests:

- `app/src/test/java/org/example/transfer_review/TransferServiceTest.java`

The starter intentionally contains defects and incomplete tests. Existing tests are part of the contract and must not be weakened, deleted, disabled, or rewritten merely to match defective production behavior.

## Business contract

### 1. Transfer request validity

A `TransferRequest` is valid only when all of these conditions hold:

- `transferId`, `sourceAccountId`, and `destinationAccountId` are not null or blank.
- Source and destination account IDs are different by value.
- `amountCents` is strictly greater than zero.
- `labels` is not null.
- Every label is non-null and nonblank.

Reject an invalid request with `IllegalArgumentException`. Do not retain or partially register it.

### 2. Request value and immutability contract

- A request represents the complete value `(transferId, sourceAccountId, destinationAccountId, amountCents, labels)`.
- Two requests with equal values for every field are equal and must have equal hash codes.
- String equality is value-based.
- The request must retain an immutable snapshot of its labels. Mutating the caller's original list after construction must not change the request, and callers must not be able to mutate the request through `labels()`.
- Shallow copying is sufficient because accepted labels are immutable strings.

### 3. Submission and idempotency

- The first valid request for a `transferId` creates exactly one `PENDING` transfer and increases `size()` by one.
- Submitting an equivalent request with the same ID is an idempotent replay: return the exact existing `Transfer` instance and do not change its state or increase `size()`.
- Submitting the same ID with a different request payload is an idempotency conflict: throw `IllegalArgumentException`, preserve the original transfer unchanged, and do not increase `size()`.
- Passing a null request to `submit` must produce `IllegalArgumentException` rather than an incidental `NullPointerException`.

### 4. State transitions

- A newly submitted transfer starts as `PENDING`.
- `approve(id)` may change only `PENDING` to `APPROVED`.
- `reject(id)` may change only `PENDING` to `REJECTED`.
- Both terminal states are final. Any later attempt to approve or reject an `APPROVED` or `REJECTED` transfer throws `IllegalStateException` and preserves its existing state.
- Approving or rejecting a null, blank, or unknown transfer ID throws `IllegalArgumentException` rather than an incidental `NullPointerException`.

### 5. Scope boundary

- Single-threaded use is sufficient; thread safety is not required in this block.
- Do not add persistence, Spring, logging, asynchronous execution, or external dependencies.
- Prefer the smallest coherent repair. Do not change public names or method signatures unless compilation requires it.

## Required tests

Keep all supplied tests and add focused coverage for at least these missing cases:

1. Blank or null required IDs.
2. Equal source and destination accounts.
3. Null labels and a null/blank label element.
4. Equality and hash-code consistency for independently created equivalent requests.
5. Same ID with a conflicting payload preserves the original transfer and service size.
6. Null request submission.
7. Unknown transfer ID transition.
8. Both terminal-state directions are protected, including repeated attempts.

Use tests to prove observable behavior. Do not test private implementation details.

## Required workflow

### 0–8 minutes — extract requirements and diagnose

Before editing production code, write a short private scratch list containing:

- violated requirements you can identify;
- predicted failing cases;
- the smallest files or methods likely to require changes.

Run the focused tests and inspect the failures. Do not begin by rewriting every class.

### 8–35 minutes — repair and add focused tests

- Fix one contract family at a time.
- Run the narrow test class frequently.
- Keep state changes failure-atomic: rejected operations must leave existing service state unchanged.

### 35–45 minutes — adversarial review

Re-read the contract and check nulls, blanks, identity versus equality, equality/hash consistency, defensive copying, idempotency conflict, unknown IDs, and terminal transitions.

### 45–50 minutes — verification and defense

Run the required commands, then prepare a two-minute explanation covering:

1. The earliest root cause you found.
2. Why your idempotency logic distinguishes replay from conflict.
3. How request immutability and equality are enforced.
4. How illegal transitions preserve state.
5. Time and space complexity of `submit`, `approve`, and `reject` under normal `HashMap` assumptions.

## Verification commands

From the `java-practice` repository root:

```bash
./gradlew :app:test --tests 'org.example.transfer_review.TransferServiceTest'
git diff --check -- \
  app/src/main/java/org/example/transfer_review \
  app/src/test/java/org/example/transfer_review
git diff -- \
  app/src/main/java/org/example/transfer_review \
  app/src/test/java/org/example/transfer_review
```

The task is not complete merely because tests pass. Reconcile the implementation against every written requirement and identify any missing test honestly.

## Scoring rubric — 20 points

### Requirement extraction — 4

- 4: identifies all major contracts and precedence/state-preservation risks before broad editing.
- 3: one minor omission.
- 2: relies mostly on visible test failures.
- 0–1: changes code without a coherent contract.

### Root-cause diagnosis — 4

- 4: locates the underlying defect families and explains their consequences precisely.
- 3: correct diagnosis with one imprecise consequence.
- 2: patches symptoms without fully identifying causes.
- 0–1: speculative or unrelated changes.

### Minimal coherent repair — 4

- 4: satisfies the contract with narrow, readable, failure-atomic changes and no unrelated edits.
- 3: correct with minor unnecessary complexity.
- 2: incomplete or materially overengineered.
- 0–1: core behavior remains wrong.

### Edge-case tests and verification — 4

- 4: supplied and required focused cases pass; tests assert state preservation as well as exceptions.
- 3: behavior passes with one noncritical evidence gap.
- 2: happy-path-heavy coverage or verification incomplete.
- 0–1: tests weakened, disabled, or still materially failing.

### Technical defense — 4

- 4: concise explanation connects requirements, fixes, failure modes, and honest complexity.
- 3: correct but misses one trade-off or consequence.
- 2: describes code without defending correctness.
- 0–1: cannot explain the resulting behavior.

Passing floor: at least 16/20, at least 3/4 in every category, no unrelated changes, and no critical correctness miss.

Critical misses include accepting invalid requests, overwriting an idempotency conflict, retaining mutable request labels, inconsistent equality/hash behavior, allowing a terminal-state reversal, or passing tests by weakening their assertions.

## Completion report

When time expires, stop editing and report:

- elapsed time;
- focused test result;
- files changed;
- your two-minute defense;
- any known gap;
- whether you used only permitted assistance.

Do not self-award the final score. The attempt is assessed after submission, and the result is recorded only after rating confirmation.
