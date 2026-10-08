# Day 2 Block 5 — Initial Answers

## 1. Git state and safe recovery
```bash
# Show unstaged changes
git diff

# Show staged changes
git diff --staged

# Stage only PaymentService.java
git add PaymentService.java

# Unstage application.yml, keeping its working-tree changes
git restore --staged application.yml

# Correct the most recent unpushed commit message
git commit --amend -m "Correct commit message"

# Verify
git status
```

`git restore --staged` changes only the staging area, so the edits in `application.yml` remain in your working tree.


## 2. Java value object
To make `TransferRequest` immutable:

* Make the class `final` and fields `private final`.
* Validate constructor inputs: required IDs and `labels` non-null, no null label elements, `amountCents > 0`, plus domain rules such as source ≠ destination if required.
* Defensively copy the list:

```java
this.labels = List.copyOf(labels);
```

There are **two mutability directions** to protect:

1. **Caller → object:** if you store the original list, the caller could later do `originalLabels.add(...)` and mutate the request indirectly.
2. **Object → caller:** if the getter exposes a mutable internal list, `request.labels().add(...)` could mutate the request.

Storing an immutable copy with `List.copyOf()` and returning that list protects both.

For `equals`/`hashCode`:

> If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` **must** be true.

The reverse is not required—different objects may have the same hash code. Both methods should use the same fields that define logical equality.

A `record` is a good fit and generates `equals`/`hashCode`, but you still need `List.copyOf(labels)` because a record is only **shallowly immutable** by default.


## 3. ACID and an external effect

Using the transfer example:

Atomicity: debit A, credit B, and insert the transfer row either all commit or all roll back.
Consistency: the transaction preserves rules/invariants, e.g. money isn't accidentally created or lost.
Isolation: concurrent transfers shouldn't interfere in ways that produce invalid results.
Durability: once committed, the transfer survives crashes/restarts.

If the remote notification succeeds but the DB transaction later rolls back, ACID cannot undo the notification. Database atomicity only covers operations participating in that database transaction; the remote service is an external side effect.

A common solution is the Transactional Outbox Pattern: write an outbox event in the same DB transaction as the transfer, commit them together, then asynchronously send the notification from the outbox.


## 4. Concurrent withdrawal and idempotency
Failing interleaving:

```text
T1 reads 100
T2 reads 100
T1 calculates 40
T2 calculates 50
T1 writes 40
T2 writes 50
```

Final balance becomes **50**, so T1’s withdrawal is lost. The correct outcome is that only one withdrawal succeeds because `60 + 50 > 100`.

One fix is **pessimistic locking**, e.g. `SELECT ... FOR UPDATE`, so only one transaction can read/update that account row at a time.

`UNIQUE(scope, request_id)` prevents the same idempotency key from creating multiple operations in the same scope.

* Same key + same request → treat as an **exact retry** and return the stored result.
* Same key + different request data → treat as **conflicting reuse** and reject it.

The unique constraint prevents duplicates; storing/comparing a request fingerprint or payload is what lets you distinguish retry from conflict.



## 5. Java concurrency and executors

`volatile` guarantees **visibility**, but `count++` is a read → modify → write operation, not one atomic operation.

```text
count = 0

T1 reads 0
T2 reads 0
T1 computes 1
T2 computes 1
T1 writes 1
T2 writes 1

Final: 1 instead of 2
```

**Repair:** use `AtomicInteger.incrementAndGet()` (or synchronization).

* **Unbounded executor queue:** tasks can accumulate faster than they're processed → memory exhaustion/high latency.
* **`submit(...)` failure:** the exception is captured in the returned `Future`; `future.get()` throws `ExecutionException`.
* **Bounded shutdown:** `shutdown()` → `awaitTermination(timeout)` → if timeout, `shutdownNow()` → `awaitTermination(...)` again. Preserve interruption by re-setting the interrupt flag with `Thread.currentThread().interrupt()` when catching `InterruptedException`.



## 6. Spring transactions and JPA
* **`REQUIRES_NEW` may not work:** Spring `@Transactional` normally works through a **proxy**. A same-object (`this.method()`) call bypasses that proxy, so the inner annotation isn't intercepted and no new transaction starts.

* **Flush ≠ commit:** `saveAndFlush()` synchronizes pending changes with the database, but the transaction is still open. A later rollback can still undo those changes.

* **Prove N+1 first:** enable SQL/query statistics and reproduce the endpoint. Look for **1 query for the parent entities + N additional queries for lazy children**. Then choose a repair such as `JOIN FETCH`, `@EntityGraph`, batching, or a DTO projection based on the access pattern.



## 7. REST validation and error contract
A reasonable contract:

* **DTO validation:** structural/input rules such as required fields, valid format, positive `amountCents`.
* **Service validation:** business rules such as account exists, sufficient balance, allowed currency/account state, and source ≠ destination.

For `POST /payments` with an `Idempotency-Key` header:

| Situation                                 | Response                                                                                                                 |
| ----------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| New payment created                       | `201 Created`                                                                                                            |
| Exact replay with same key + same request | Return the **same logical result** without processing again, commonly `200 OK` or replay the original `201` consistently |
| Same key + different request              | `409 Conflict`                                                                                                           |
| Malformed JSON / DTO validation failure   | `400 Bad Request`                                                                                                        |
| Business rule violation                   | Appropriate stable `4xx`, e.g. `422 Unprocessable Content`                                                               |

Use a stable error shape, for example:

```json
{
  "code": "IDEMPOTENCY_CONFLICT",
  "message": "Idempotency key was already used for a different request",
  "details": {}
}
```

The **database is the correctness boundary**: enforce something like:

```sql
UNIQUE(scope, idempotency_key)
```

and store the request fingerprint/result transactionally with the payment. Application-level `if (!exists)` checks alone are race-prone under concurrent requests.



## 8. Microservices versus modular monolith
I would choose a **modular monolith**, with disputes implemented as a clearly separated module.

**Trade-offs:**

* **Simpler operations:** one deployment, no service discovery or distributed tracing complexity.
* **Simpler consistency:** payment/dispute operations can use local database transactions instead of distributed consistency mechanisms.
* **Lower independence:** the dispute module cannot be deployed or scaled separately—but that's not currently required.
* Strong module boundaries are still necessary to prevent the monolith becoming tightly coupled.

**Signals to extract a microservice later:**

1. Dispute traffic requires **independent scaling** from the rest of the banking backend.
2. A separate team needs **independent ownership and deployment cadence**.

**Principle:** don't pay the operational and distributed-system cost of microservices until you have a concrete reason to need that boundary.



## Corrections after scoring

### 1. Git corrections

```bash
# Show unstaged and staged changes.
git diff
git diff --staged

# Stage only the intended Java file.
git add PaymentService.java

# Unstage the configuration file while preserving its working-tree edits.
git restore --staged application.yml

# Correct the latest unpushed commit message.
git commit --amend -m "Corrected message"

# Verify the resulting state and latest commit.
git status
git diff
git diff --staged
git log -1 --oneline
```

If the bad commit has already been shared, prefer a new `git revert <commit>` commit instead of rewriting the shared history. Revert preserves the history teammates may already depend on and records an explicit inverse change. Amend or rebase is appropriate only when rewriting the commit cannot disrupt other collaborators.

### 2. `TransferRequest` equality and immutability corrections

Two requests are equal when all value-defining fields are equal: `requestId`, source account ID, destination account ID, `amountCents`, and `labels`. Use value equality for object fields and ordinary primitive comparison for `amountCents`. Equal objects must always produce the same hash code from the same fields; unequal objects may still collide.

The constructor must validate first and retain a defensive immutable snapshot such as `List.copyOf(labels)`. This prevents later mutation of the caller's original list from changing the request. The accessor must return that immutable stored list or another immutable copy so callers cannot mutate the request through the returned reference. The class and fields should remain final, and label elements must satisfy the required non-null/non-blank domain rules.
