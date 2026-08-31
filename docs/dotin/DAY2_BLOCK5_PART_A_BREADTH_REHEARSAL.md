# Day 2 Block 5 — Part A Breadth Rehearsal

## Conditions

- Total: 50 minutes
- First 40 minutes: closed book, no internet, AI, IDE search, or notes
- Eight questions, maximum five minutes each
- Type concise exam answers in `DAY2_BLOCK5_ANSWERS.md`
- Stop each answer when its five minutes expires and move on
- Final 10 minutes: keep the initial answers unchanged and add corrections only after scoring

This rehearses plausible mid-level Java-backend breadth. It is representative practice, not a claim about Dotin's private question bank.

## Questions — 5 points each

### 1. Git state and safe recovery

`PaymentService.java` has wanted unstaged edits. `application.yml` was accidentally staged. A previous local commit also has the wrong message but has never been pushed.

Give the commands that:

- show unstaged versus staged changes;
- stage only `PaymentService.java`;
- unstage `application.yml` without discarding its working-tree edits;
- safely correct the unpushed commit message.

Then state what recovery strategy you would prefer if the bad commit had already been shared with teammates, and why.

### 2. Java value object

A `TransferRequest` contains `requestId`, source/destination account IDs, `amountCents`, and `List<String> labels`.

Explain how to make it immutable and give the essential `equals`/`hashCode` contract. Include constructor validation and both directions through which the labels list could otherwise expose mutability.

### 3. ACID and an external effect

A service debits account A, credits account B, writes a transfer row, and then calls a remote notification service.

Explain ACID using this example. If the notification succeeds but the database transaction later rolls back, explain why database atomicity does not undo the notification and name one responsible design approach.

### 4. Concurrent withdrawal and idempotency

An account has 100. T1 and T2 both read 100; T1 withdraws 60 and T2 withdraws 50. Each writes its calculated balance.

Show the failing interleaving and correct outcome. Choose one database mechanism that prevents the invalid result. Then explain how `UNIQUE(scope, request_id)` helps distinguish an exact retry from conflicting reuse of the same idempotency key.

### 5. Java concurrency and executors

Why does `volatile int count` not make `count++` safe? Give a failing interleaving and one correct repair. Then state:

- one danger of an unbounded executor queue;
- how a failure from `submit(...)` is observed;
- the bounded shutdown sequence.

### 6. Spring transactions and JPA

A transactional service method calls another method on the same object annotated `REQUIRES_NEW`, calls `saveAndFlush`, and returns entities whose lazy children are accessed by the controller.

Explain:

- why the inner annotation may not start a new transaction;
- why flush is not commit;
- how you would prove N+1 before choosing a query repair.

### 7. REST validation and error contract

Design the HTTP contract for `POST /payments` with an idempotency key. Cover:

- DTO validation versus service business validation;
- successful creation and exact replay responses;
- conflicting key reuse;
- malformed/invalid input;
- stable error-response shape;
- the database correctness boundary.

You do not need to write controller code.

### 8. Microservices versus modular monolith

A small team is adding a new payment-dispute capability to an existing banking backend. Traffic and team ownership do not yet require independent scaling or deployment.

Choose either a modular monolith or a new microservice. Defend the choice with at least three trade-offs, and state two concrete signals that would justify changing the boundary later.

## Scoring

- 5 points per question = 40
- Target: **32/40**
- A critical misconception in transaction, idempotency, concurrency, or shared-Git recovery cannot be hidden by stronger answers elsewhere.

