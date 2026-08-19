# Java Backend Course A05 - Task Import Pipeline

## Prepared material

This task assesses Effective Java Items:

- 42-48
- 54-55
- 69
- 73-75
- 77

Item 76 is not part of this A05 assessment.

Keep the book, notes, previous implementations, and answer material closed while designing and implementing the task.

## Environment

- Project: `/Users/kavrin/Projects/java-learning/java-practice`
- Java: 21
- Package: `org.example.task_import`
- Testing: the project's existing JUnit and Gradle setup
- Do not add external dependencies.

## Objective

Build a task-import component that transforms raw task rows into an immutable import plan.

Implement the same transformation independently in two styles:

1. An imperative implementation using ordinary iteration.
2. A stream implementation using a side-effect-free pipeline.

The implementations may share:

- Immutable domain types.
- Comparators.
- Pure validation and normalization helpers.
- Exception types.

One transformation must not call or delegate to the other.

## Input model

Each input row contains:

- `UUID id`
- `String title`
- `int priority`
- `List<String> labels`

Define a `TaskRowSource` abstraction whose loading operation can throw `IOException`.

Choose and justify the public API signatures before implementation.

## Output model

Return an immutable `TaskImportPlan` containing:

- An immutable collection of imported tasks.
- An immutable collection of aggregate labels.
- A lookup operation:

```java
Optional<ImportedTask> findById(UUID id)
```

Public sequence-returning APIs must return reusable immutable collections, not `Stream`, arrays, mutable internal collections, or `null`.

## Validation requirements

Reject a batch when it contains:

- A null row.
- A null task ID.
- A null title.
- A blank title after trimming.
- A null label collection.
- A null label element.
- A duplicate task ID.

Validation errors must be distinguishable from infrastructure failures.

Do not use exceptions for ordinary traversal, lookup absence, loop termination, or expected branching.

## Transformation requirements

For every valid row:

1. Trim the title.
2. Normalize labels by:
   - Trimming whitespace.
   - Converting to lowercase with `Locale.ROOT`.
   - Discarding blank labels.
   - Removing duplicates while preserving their first occurrence.
3. Preserve arbitrary integer priorities; do not impose an invented range.
4. Order imported tasks by:
   - Higher priority first.
   - Ascending UUID as the tie-breaker.
5. Build the aggregate label collection by:
   - Traversing tasks in their final sorted order.
   - Traversing each task's normalized labels in order.
   - Keeping only the first occurrence of each label.

Input collections must not be mutated.

Returned domain objects and collections must not expose mutable internal state.

## Lambda and functional-interface requirements

The implementation must demonstrate the following deliberately:

- Use at least one lambda to represent a small function object.
- Replace at least one lambda with a method reference where the method reference is clearer.
- Use suitable standard interfaces from `java.util.function` where a functional interface is needed.
- Do not create a custom duplicate of `Predicate`, `Function`, `Consumer`, `Supplier`, `UnaryOperator`, `BinaryOperator`, or another suitable standard interface.
- If a custom functional interface is genuinely required, document and defend its domain-specific contract.
- Keep lambdas small and readable.
- Use an anonymous class only if a lambda cannot correctly express the required behavior; otherwise explain why it is unnecessary.

## Imperative implementation

The imperative version must:

- Use clear loops and local state.
- Make validation, normalization, duplicate detection, sorting, and aggregation explicit.
- Avoid exceptions as control flow.
- Return the complete immutable plan.
- Remain understandable without translating stream operations mentally.

## Stream implementation

The stream version must:

- Express the transformation as meaningful pipeline stages.
- Keep functions passed to stream operations side-effect-free.
- Never mutate an external collection, map, counter, or other captured state.
- Not use `peek` for validation, mutation, or business logic.
- Use collectors or other appropriate reductions for result construction.
- Use `map` when one element becomes one element.
- Use `flatMap` only when one element produces zero or more elements.
- Preserve the required deterministic ordering.
- Return the same result as the imperative implementation.

Internal mutation performed by a correctly used collector is allowed; mutation of state external to the pipeline is not.

## Return-type and Optional requirements

- Empty input must return a valid plan containing empty immutable collections.
- Never return `null` instead of an empty collection.
- `findById` must return `Optional.empty()` when the ID is absent.
- Do not return `Optional` for collections.
- Do not accept `Optional` as a parameter.
- Do not store `Optional` inside collections.
- Do not call `Optional.get()` unless presence has already been logically established; prefer safer operations where appropriate.
- Null arguments to public lookup methods must follow an explicit, documented contract.

## Exception requirements

### Ordinary versus exceptional conditions

Exceptions are for exceptional conditions, not:

- Loop termination.
- Missing lookup results.
- Duplicate filtering.
- Normal validation decisions.
- Stream branching.

### Abstraction boundary

When `TaskRowSource` throws `IOException`:

- Catch it only at the task-import service boundary.
- Translate it to an exception appropriate to the task-import abstraction.
- Preserve the original `IOException` as the cause.
- Do not expose a low-level storage or file exception as the primary public abstraction.
- Do not catch and translate unrelated programming errors.

### Documentation

Every exposed API element must document:

- Each checked exception it declares.
- Every relevant unchecked exception it can throw.
- The exact condition that produces each exception.

Use precise `@throws` documentation.

Do not declare broad types such as:

```java
throws Exception
```

### Diagnostic messages

Validation and import exceptions must include useful, safe failure context where available, such as:

- Row index.
- Task ID.
- Field name.
- Rejected non-sensitive value.
- Operation being performed.

Messages must explain the failure rather than merely state `invalid input` or `import failed`.

Do not include secrets or dump an entire potentially sensitive row.

### No ignored exceptions

Every `catch` block must do one of the following:

- Handle the condition.
- Translate it.
- Propagate it.
- Explicitly justify why ignoring it is correct.

Empty catch blocks are forbidden.

Translated exceptions must preserve their causes.

## Parallel-stream decision

The production implementation must remain sequential unless you demonstrate both:

1. Correctness under parallel execution.
2. A realistic measured speed improvement.

Do not add `.parallel()` or use `parallelStream()` merely because the API supports it.

Your defense must address:

- Encounter and result ordering.
- Stateful operations.
- Collector safety.
- Reduction correctness.
- Exception behavior.
- Dataset size and per-element work.
- Splitting characteristics.
- Measurement methodology.

A timing claim without a realistic measurement is not evidence.

## Required tests

Write tests for at least the following cases.

### Empty and basic input

- Empty input returns a non-null plan.
- Its task and label collections are empty and immutable.
- A single valid row is normalized correctly.

### Validation

- Null row.
- Null ID.
- Null title.
- Blank title.
- Null label collection.
- Null label element.
- Duplicate IDs.
- Invalid data appearing after one or more valid rows.

### Normalization

- Titles are trimmed.
- Labels are trimmed.
- Labels use lowercase with `Locale.ROOT`.
- Blank labels are discarded.
- Duplicate labels within one task preserve their first occurrence.
- Duplicate labels across tasks appear once in the aggregate collection.

### Ordering

- Higher priority appears first.
- Equal priorities use ascending UUID.
- Aggregate labels follow final task order.
- Results are deterministic across repeated executions.

### Optional

- Existing ID returns the task.
- Missing ID returns `Optional.empty()`.
- Empty-plan lookup returns `Optional.empty()`.
- The API never returns a null `Optional`.

### Infrastructure failure

- `TaskRowSource` throws `IOException`.
- The service throws the task-import abstraction exception.
- The translated exception preserves the exact original cause.
- Its message includes useful operation context.
- The `IOException` is not silently ignored.

### Exception diagnostics

- Validation failures identify the relevant row and field.
- Duplicate-ID failures include the duplicated ID.
- Messages contain sufficient safe context to diagnose the problem.
- Public exception contracts match their Javadoc.

### Immutability

- Mutating original input collections after import cannot alter the plan.
- Returned task collections cannot be mutated.
- Returned label collections cannot be mutated.
- Per-task label collections cannot be mutated.

### Implementation equivalence

For every valid scenario:

- The imperative and stream implementations return equal plans.
- They produce the same task ordering.
- They produce the same normalized labels.
- They produce the same lookup behavior.

For invalid scenarios:

- They reject the same inputs using the same public exception contract.

## Verification commands

Run the focused A05 tests using the project's Gradle wrapper. Record the exact command and complete outcome.

Then run the full project test suite and record its outcome separately.

Do not describe the work as verified unless the relevant commands actually pass.

## Complexity analysis

State the time and auxiliary-space complexity for both implementations.

Your analysis must account for:

- Reading all rows.
- Validation.
- Duplicate detection.
- Label normalization and deduplication.
- Sorting.
- Aggregate-label construction.
- Lookup-index construction.

Do not state only `O(n)` if sorting or total label volume changes the bound.

Define every variable used in the analysis.

## Required design checkpoint before coding

Before implementing, provide:

1. Proposed public types and method signatures.
2. The imperative algorithm.
3. The stream pipeline stages.
4. The central invariants and abstraction boundaries.
5. Standard functional interfaces you intend to use.
6. The lambda that demonstrates Item 42.
7. The method reference that demonstrates Item 43.
8. Collection-return and `Optional` decisions.
9. Exception types and translation boundary.
10. Diagnostic-message contents.
11. Sequential-versus-parallel decision.
12. Expected time and space complexity.
13. Verification plan.

No hints or answer implementation should be opened before this design attempt.

## Interview defense

After implementation, be prepared to explain:

- Lambda versus anonymous class.
- Method reference versus lambda.
- Standard versus custom functional interface.
- When a loop is clearer than a stream.
- When a stream is clearer than a loop.
- Intermediate versus terminal operations.
- Stateless versus stateful operations.
- Stream laziness.
- `map` versus `flatMap`.
- Why the pipeline is side-effect-free.
- Collection versus `Stream` as a public return type.
- Why the implementation is sequential.
- Empty collections versus `null`.
- Appropriate and inappropriate uses of `Optional`.
- Exceptions versus ordinary control flow.
- Exception translation and chaining.
- Checked and unchecked exception documentation.
- Useful diagnostic detail messages.
- Why every catch block exists.

## Changed-review assessment

After the implementation and defense, review a separate flawed example containing several of these defects:

- Unnecessary anonymous class.
- Less-readable lambda where a method reference is clearer.
- Custom functional interface duplicating a standard one.
- Overused stream pipeline.
- Hidden mutation inside a pipeline.
- Business logic inside `peek`.
- Unjustified parallel stream.
- Public method returning a one-shot stream unnecessarily.
- Nullable collection return.
- Unsafe or unnecessary `Optional`.
- Exception-driven ordinary control flow.
- Leaked low-level exception.
- Missing `@throws` documentation.
- Context-free exception message.
- Swallowed exception.

Correctly identifying and repairing this changed example without an `H2` or `H3` hint is part of the A05 exit evidence.

## Completion criteria

A05 is complete only when:

- The project compiles.
- Focused and full tests pass.
- Imperative and stream implementations agree.
- Every prepared Effective Java item has observable evidence.
- Complexity analysis is correct.
- The interview defense is correct.
- The changed-review assessment is completed without `H2` or `H3` help.
- Exact verification commands and results are recorded.

If any exit requirement remains incomplete, close A05 honestly with the observed evidence and route only the remaining gap to `A05-R1`.
