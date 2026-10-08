# Reflection learning path: annotation-driven configuration binder

## Target performance

Implement `ReflectiveConfigBinder.bind` using only the Java reflection API. At the end, you should be able to discover and access runtime metadata, constructors, fields, superclass members, and lifecycle methods—and explain the failures reflection wraps or exposes.

The implementation is intentionally absent. Tests provide the behavioral contract without containing the solution.

## Working agreement

- Work on one step only; do not run the later step tests early.
- Before coding a step, answer its prediction question without references.
- Make the current step green, then rerun every completed step to detect regression.
- Do not change annotations, fixtures, or assertions to make a test pass.
- Do not hard-code fixture class, field, method, or property names.
- Use only the JDK; do not add a bean mapper, serializer, or dependency-injection library.
- Reading and green tests are evidence of preparation and implemented behavior, not yet evidence of independent mastery.

No deadline was supplied. Expect roughly 45–90 focused minutes per implementation step, but stop at the stated boundary rather than rushing into the next one.

## Capability map

| Step | Capability | Main APIs | Observable evidence |
| --- | --- | --- | --- |
| 0 | Read annotation metadata | `Retention`, `Target`, `Class.getAnnotation` | Metadata test passes and you can explain runtime retention |
| 1 | Construct an unknown type | `getDeclaredConstructor`, `trySetAccessible`, `newInstance` | Private construction and constructor failures behave correctly |
| 2 | Discover and write fields | `getDeclaredFields`, `Field.getType`, `Field.set` | Required and optional private string fields bind correctly |
| 3 | Convert using runtime types | primitive/wrapper `Class` objects, `Class.isEnum` | Supported scalar types convert; malformed values fail clearly |
| 4 | Traverse and validate metadata | `getSuperclass`, `Modifier`, annotation values | Inherited fields work; invalid metadata is rejected |
| 5 | Invoke lifecycle methods | `getDeclaredMethods`, `Method.invoke`, `InvocationTargetException` | Callback discovery, validation, and failure unwrapping work |
| 6 | Integrate the complete binder | all previous APIs | One realistic configuration exercises the whole pipeline |
| 7 | Transfer to a changed case | design and new tests | You extend behavior without weakening the original contract |

## Step 0 — Understand runtime annotations

### Preparation

Read the Java API documentation for:

- `Retention` and `RetentionPolicy.RUNTIME`
- `Target` and `ElementType`
- `AnnotatedElement.getAnnotation`

### Prediction before running

Without opening the annotation source, predict whether `ConfigProperty` and `AfterBinding` can be found at runtime and where each is legal.

### Run

```bash
./gradlew test --tests org.example.reflection_config.Step0AnnotationMetadataTest
```

### Done when

- The test passes without changing production code.
- You can explain why an annotation with `RetentionPolicy.SOURCE` would be invisible to the binder.

Stop here and record your explanation before Step 1.

## Step 1 — Construct a class you only know at runtime

### Scope

Make `bind` return a newly constructed object. Do not inspect or populate fields yet.

### Requirements

- Reject a null target type with a clear `IllegalArgumentException`.
- Locate the target type's no-argument constructor.
- Allow a private constructor using `trySetAccessible()`.
- Reject a type without a no-argument constructor using `ConfigurationException`.
- If a constructor itself throws, unwrap `InvocationTargetException` and preserve the constructor's real exception as the cause.

### Prediction before coding

What exception does `Constructor.newInstance()` throw when the constructor body throws, and where is the original exception stored?

### Run

```bash
./gradlew test --tests org.example.reflection_config.Step1ConstructionTest
```

### Done when

- All three Step 1 tests pass.
- Step 0 still passes.
- `bind` contains no fixture-specific branches.

## Step 2 — Bind private string fields

### Scope

Discover `@ConfigProperty` on fields declared directly by the target class. Support only `String` in this step. Do not traverse superclasses or invoke callbacks yet.

### Requirements

- Reject a null property map with a clear `IllegalArgumentException`.
- Read the annotation's key and `required` flag.
- Make an annotated private field accessible.
- Assign a present string property.
- Report an absent required key in `ConfigurationException`.
- Leave an absent optional field untouched so its initializer/default survives.

### Prediction before coding

Why would assigning `null` to an absent optional field violate the exercise contract even when the field type is `String`?

### Run

```bash
./gradlew test --tests org.example.reflection_config.Step2FieldBindingTest
```

### Done when

- Step 2 passes.
- Steps 0–1 still pass.
- Missing and inaccessible fields fail with binder-level context rather than an unexplained reflection exception.

## Step 3 — Convert values using `Class<?>`

### Scope

Separate string conversion from field discovery. Add all supported scalar types, but keep discovery limited to the target class.

### Supported targets

- `String`
- `int` and `Integer`
- `long` and `Long`
- `boolean` and `Boolean`
- any enum type

Boolean input must be exactly `true` or `false`, case-insensitively. Enum input uses the exact constant name.

### Failure behavior

- A malformed value must produce `ConfigurationException` containing the property key.
- Preserve a useful underlying cause such as `NumberFormatException`.
- An unsupported type must produce `ConfigurationException` containing the fully qualified type name.

### Prediction before coding

Are `int.class` and `Integer.class` equal? How will that affect a conversion dispatch based on `Field.getType()`?

### Run

```bash
./gradlew test --tests org.example.reflection_config.Step3TypeConversionTest
```

### Done when

- Step 3 passes.
- Steps 0–2 still pass.
- Conversion is isolated enough that field discovery does not need to understand parsing details.

## Step 4 — Traverse inheritance and reject dangerous metadata

### Scope

Walk from the target class through each superclass, stopping before `Object`. Validate the complete field model before performing dangerous writes.

### Requirements

- Include annotated fields declared by superclasses.
- Reject duplicate property keys anywhere in the hierarchy.
- Reject annotated `static` fields and do not mutate global state.
- Reject annotated `final` fields.
- Unknown keys in the input map may be ignored.

### Prediction before coding

Why does `getDeclaredFields()` require manual superclass traversal, while `getFields()` still does not satisfy this exercise?

### Run

```bash
./gradlew test --tests org.example.reflection_config.Step4HierarchyAndValidationTest
```

### Done when

- Step 4 passes.
- Steps 0–3 still pass.
- Validation is based on `Modifier` and annotations, not field names.

## Step 5 — Discover and invoke a lifecycle callback

### Scope

After all fields have been populated, discover and invoke at most one `@AfterBinding` method across the complete class hierarchy.

### Requirements

- The callback may be private or inherited.
- It must be an instance method with zero parameters.
- Multiple callbacks are invalid.
- Invoke it only after every field assignment completes.
- When it throws, `ConfigurationException.getCause()` must be the callback's real exception, not `InvocationTargetException`.

### Prediction before coding

If a private callback is declared by a superclass, can its `Method` be invoked with a subclass instance? Explain why before testing it.

### Run

```bash
./gradlew test --tests org.example.reflection_config.Step5LifecycleCallbackTest
```

### Done when

- Step 5 passes.
- Steps 0–4 still pass.
- Callback signature validation occurs before invocation.

## Step 6 — Run the integration case

This step combines private construction, inherited fields, primitive and wrapper conversion, enum conversion, an absent optional property, and a private lifecycle callback.

### Prediction before running

Write the exact order of your binder pipeline. Identify which phases discover metadata, validate it, construct the object, mutate it, and invoke user code.

### Run the integration test

```bash
./gradlew test --tests org.example.reflection_config.Step6FullBinderTest
```

### Run the entire reflection package

```bash
./gradlew test --tests 'org.example.reflection_config.*'
```

### Done when

- Every reflection test passes together.
- You can explain why validation before mutation reduces partial-state risk.
- You can explain the access and module-boundary limitations of `trySetAccessible()`.

## Step 7 — Changed-case transfer

Do this only after the original suite is green, without using your previous implementation as a script.

1. Add support for `double` and `Double`.
2. Add tests for both successful conversion and malformed input.
3. Add a malformed configuration containing a `static @AfterBinding` method and define the rejection behavior.
4. Explain where reflection metadata could be cached in production and what class-loader or cache-lifetime risk that introduces.

Passing Step 7 demonstrates transfer to changed requirements; it is stronger evidence than repeating the original cases.

## Final contract summary

The completed binder must:

1. Reject null target types and property maps.
2. Construct through a possibly private no-argument constructor.
3. Discover annotated fields and methods across the hierarchy, excluding `Object`.
4. Validate duplicate keys, forbidden modifiers, and callback signatures.
5. Convert supported values and preserve optional defaults.
6. Populate every field before invoking user callback code.
7. Translate reflection failures into contextual `ConfigurationException` instances while preserving meaningful underlying causes.

## Readiness gate

Implementation readiness requires the complete suite to pass. Reflection understanding additionally requires an unaided explanation of:

- `getDeclaredX` versus public/inherited `getX` APIs;
- manual superclass traversal;
- primitive versus wrapper `Class<?>` values;
- `trySetAccessible()` and named-module boundaries;
- why reflective constructor and method calls use `InvocationTargetException`;
- why metadata validation should precede mutation and callback execution.

If any explanation is weak, revisit only that step, describe the failed mental model, correct it, and test it later with a changed example.
