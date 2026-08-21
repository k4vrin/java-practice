# A05 evidence: retrospective design checkpoint

This note records the design and verification reasoning after the imperative and stream importers were implemented. It is not a pre-coding design checkpoint.

## Public API

- `TaskImporter#importTasks(TaskRowSource) -> TaskImportPlan` is the input port. A null source throws `NullPointerException`; malformed rows throw `TaskImportValidationException`; source `IOException` failures become `TaskImportException` with the original cause retained.
- `TaskRowSource#load() -> List<TaskRow>` is the outbound port and declares `IOException` exactly when its underlying source cannot be read or decoded into a row batch.
- `TaskImportPlan#getTasks()` and `getAggregateLabels()` return reusable immutable `List` values. `findById(UUID)` returns `Optional.empty()` when absent and throws `NullPointerException` for a null ID.
- `ImportedTask` is immutable. Its constructor rejects null ID, title, label list, and label elements, and rejects a blank title. `TaskImportPlan` rejects null collections/elements and cannot build an unambiguous lookup index for duplicate task IDs.

## Algorithms and invariants

Both importers preserve these invariants:

- A successful import returns one immutable plan; input collections are never mutated.
- Validation occurs before transformation: rows, IDs, titles, labels, label elements, and duplicate IDs are checked with row-aware diagnostics.
- Titles are trimmed. Labels are trimmed, lowercased with `Locale.ROOT`, blank labels are removed, and first normalized occurrences are retained.
- Tasks are ordered by descending priority then ascending UUID. Aggregate labels traverse that final order and retain their first occurrence.

The imperative service loads rows at the application boundary, validates them in explicit loops, constructs each `ImportedTask`, sorts the task list, then builds aggregate labels with a `LinkedHashSet`.

The stream service uses the same validation boundary, then applies these pure stages:

1. `map(StreamTaskImporter::toImportedTask)` performs one row-to-task conversion.
2. `sorted(ImportedTask.COMPARATOR)` establishes final task order.
3. `flatMap(task -> task.labels().stream()).distinct()` creates ordered aggregate labels.

`toImportedTask` and `normalizeLabels` are pure helpers. No stream stage mutates captured external state.

## Functional-interface choices

`TaskRowSource` is a custom `@FunctionalInterface` because it is a domain-specific outbound port whose checked `IOException` is part of the architecture boundary. A `Function` or `Supplier` would either lose that checked exception or misrepresent source loading.

Standard functional interfaces are used where they fit: `Comparator` backs `ImportedTask.COMPARATOR`, and the stream pipeline uses method references such as `StreamTaskImporter::toImportedTask` and `String::trim`. The small predicate lambda `label -> !label.isEmpty()` reads more clearly than an artificial helper. No anonymous class is needed.

## Collections, Optional, and exceptions

`List.copyOf` and stream `toList()` prevent exposure of mutable internal collections. `Optional` is used only for one-item lookup absence; collections are never wrapped in `Optional` and neither importer returns `null` for a successful result.

`TaskImportValidationException` represents malformed batch data. `TaskImportException` translates only an `IOException` at the source-loading boundary, using the safe diagnostic `Task import failed while loading task rows` and preserving the exact original cause. Unchecked programming errors are not translated. Diagnostics include safe row, task-ID-when-known, field, and reason context; they do not dump source rows or source contents.

## Complexity

Let `R` be the number of rows, `L` the total number of input label elements across all rows, `l_i` the labels in row `i`, `A` the number of distinct aggregate labels (`A <= L`), and `C` the total character volume of every input title and label. `C` matters because trimming, lowercasing, string hashing, and string equality examine characters. UUID comparison, hashing, and equality are constant-time for this analysis. Source-reading time and storage are implementation-specific to `TaskRowSource`; the importer analysis below begins after `load()` returns its materialized list.

| Stage | Time | Auxiliary space beyond returned plan |
| --- | --- | --- |
| Validate rows and detect duplicate IDs | `O(R + L + C)` | `O(R)` for task IDs |
| Normalize titles and labels | `O(L + C)` | `O(max l_i)` transient per-row distinct state; normalized character output is `O(C)` |
| Sort imported tasks | `O(R log R)` | `O(R)` sorting references/implementation buffer |
| Build aggregate labels | expected `O(L + C)` | `O(A)` distinct-label state |
| Build lookup index | `O(R)` | `O(R)` |

Under the normal expected-time behavior of Java hash tables, excluding source loading, both implementations take `O(R log R + L + C)` time. Their auxiliary reference/state space is `O(R + A + max l_i)` in addition to the returned plan; the normalized strings add `O(C)` character storage. The returned plan's task, per-task-label, aggregate-label, and lookup structures occupy `O(R + L + A)` references plus `O(C)` normalized-character storage.

The imperative version makes these states explicit through sets and lists. The stream version has the same asymptotic bounds: validation has an ID set, `distinct()` retains ordered deduplication state, `toList()` materializes tasks, and the plan builds the lookup map.

## Sequential-stream decision

The stream implementation stays sequential. Encounter order is part of the contract for per-task and aggregate labels, and the final result additionally requires deterministic priority/UUID ordering. `distinct()` is stateful; sequential ordered execution makes first-occurrence semantics direct. Its reduction has no external mutation: stream operations create task values, and aggregate deduplication is performed by the ordered stream reduction.

Parallel execution would require separate evidence for ordered stateful operations, collector safety, reduction correctness, source and validation exception propagation, and the cost of splitting. The expected dataset is a bounded task batch with modest per-row work, so sorting, label normalization, synchronization/merging, and splitting overhead can dominate. No realistic workload or measurement has demonstrated a speedup, so a parallel implementation is not justified. Any future change must measure representative source sizes and label distributions, warm-up, repeated runs, wall-clock variance, allocation/GC effects, and preserve the current contract tests.

## Verification

The earlier verification baseline requested during review was:

```text
rtk ./gradlew test --rerun-tasks --tests 'org.example.task_import.*'
172 tests, 0 failures, 0 errors, 0 skipped

rtk ./gradlew test --rerun-tasks
207 tests, 0 failures, 0 errors, 0 skipped
```

The documented null-boundary tests added after that review increase those counts. The following current commands and results were recorded after this retrospective checkpoint:

```text
rtk ./gradlew test --rerun-tasks --tests 'org.example.task_import.*'
BUILD SUCCESSFUL
186 tests, 0 failures, 0 errors, 0 skipped

rtk env JAVA_TOOL_OPTIONS="-Duser.language=tr -Duser.country=TR" ./gradlew test --rerun-tasks
BUILD SUCCESSFUL
221 tests, 0 failures, 0 errors, 0 skipped

rtk ./gradlew test --rerun-tasks
BUILD SUCCESSFUL
221 tests, 0 failures, 0 errors, 0 skipped

rtk ./gradlew :app:javadoc
BUILD SUCCESSFUL (task-import emitted no Javadoc warnings; unrelated sample and task-registry warnings remain)
```

The full suite includes the shared validation, normalization, ordering, aggregate-label, Optional, immutability, and exception contracts twice: once for `ImperativeTaskImporter` and once for `StreamTaskImporter`. The direct equivalence suite remains in addition to those independent contract runs.
