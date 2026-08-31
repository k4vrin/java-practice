# Day 2 Block 3 — Java Concurrency and Executors

## Conditions

- Timed work: 50 minutes
- Closed book after the prescribed primer: no internet, AI, or notes
- Work only in `org.example.concurrency_repair` and this block's notes
- Preserve all public method signatures
- Do not weaken, delete, or skip tests

## Scenario

A payment worker processes requests concurrently. Operations relies on two in-memory metrics:

- the total number of handled requests;
- the number of failures for each failure code.

The supplied implementations lose updates under contention. `ConcurrentHashMap` is already used for the frequency data, but the overall update is still incorrect.

## Requirements

1. `RequestCounter.increment()` must retain every increment when called concurrently.
2. `RequestCounter.value()` must return the current count safely.
3. `FailureFrequency.record(code)` must retain every increment for the same code when called concurrently.
4. Different failure codes must remain independent.
5. A null or blank failure code must throw `IllegalArgumentException` without modifying state.
6. `snapshot()` must return an immutable snapshot that later updates cannot change.
7. Use standard Java 21 concurrency mechanisms; do not serialize the test harness or replace concurrency with a single-thread executor.
8. Removing `Thread.yield()` alone is not a repair. Your correctness argument must hold for every interleaving.

## Required process

### Minutes 0–5: diagnosis

Before editing production code, fill in the first four prompts in `DAY2_BLOCK3_NOTES.md`. Run the focused test and record the observed expected and actual counts.

### Minutes 5–25: repair

Repair both lost-update defects with the smallest clear change. Keep validation and snapshot behavior correct.

### Minutes 25–37: focused tests

Make the supplied tests green. Add at least two focused tests:

- multiple failure codes updated concurrently;
- snapshot immutability and snapshot isolation from later updates.

### Minutes 37–50: executor defense

Answer the remaining notes without using references. Keep each answer to two or three sentences.

## Commands

```bash
./gradlew :app:test --tests 'org.example.concurrency_repair.ConcurrencyRepairTest'
git diff --check
```

## Acceptance criteria

- All focused tests pass repeatedly.
- The repair provides a real atomicity or guarding argument.
- Invalid input does not change frequency state.
- Returned snapshots are immutable and detached from later updates.
- The notes correctly address bounded queues, saturation, failure observation, cooperative cancellation, and shutdown.
- No claim says `volatile` makes `counter++` atomic or that thread-safe individual map methods make an arbitrary sequence atomic.

## Scoring — 10 points

- Counter diagnosis and repair: 2
- Compound map diagnosis and repair: 2
- Added adversarial tests: 2
- Executor reasoning: 3
- Clarity and scoped changes: 1

Target: **8/10**. Either remaining lost update or either critical misconception is a critical miss.
