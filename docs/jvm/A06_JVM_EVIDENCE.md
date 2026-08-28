# A06: JVM conceptual model and observations

## A. Annotated JVM sketch

This is a **conceptual model**, not proof of physical object placement, object header layout, garbage-collector implementation, JIT escape analysis, or other JVM optimizations.

```text
Class metadata / method area concept
  OrderProcessor class metadata: methods process(), validate()
  Order class metadata: fields labels, connection
  (The JVM's actual metadata representation is implementation-specific.)

Thread "http-1"                         Thread "http-2"
stack                                    stack
  process(orderRef) frame                  process(orderRef) frame
    orderRef ───────────────┐                 orderRef ───────────────┐
    local auditRef ──────┐  │                 local reportRef ──────┐  │
  run() frame            │  │               run() frame            │  │
                         │  │                                      │  │
                         ▼  ▼                                      ▼  ▼
heap (conceptual)
  Order #42 { labels ───► List ["A", "B"], connection ───► OpenConnection }
  AuditEvent { order ─────────────────────────────────────► Order #42 }
  longLivedAuditLog ───► List ───► AuditEvent
                                 │
                                 └─ This reference chain keeps Order #42 reachable
                                    even after both method-local references disappear.

  OpenConnection { OS socket/file descriptor, closed=false }
    ^ reachable does not mean it will be closed soon; GC reachability is a memory-liveness
      question. The resource owner should close it deterministically (for example with
      try-with-resources), releasing the external resource independently of collection.
```

## B. Class-initialization probes

Source: `ClassInitializationScenarios` and `ClassInitializationScenariosTest`.

Prediction and assertions:

| Scenario | Prediction | Asserted event output |
| --- | --- | --- |
| Read `Child.COMPILE_TIME_CONSTANT` | No class initialization; the constant is inlined at the use site. | `events=[]` |
| Read `Child.PARENT_NON_CONSTANT` | Only `Parent` initializes because it declares the field. | `events=[parent initialized]` |
| Read `Child.CHILD_NON_CONSTANT` | `Parent` initializes before `Child`. | `events=[parent initialized, child initialized]` |
| Read the child field twice | Initializers run once after successful initialization. | `events=[parent initialized, child initialized]` |

Every JUnit assertion starts a new `java` process. This intentionally avoids test-order dependence and prevents an earlier scenario from initializing either fixture class in the JVM used by a later scenario.

## C. Runtime observation

The probe starts `ThreadObservationProgram`, waits until its named worker has started, and then inspects that JVM. Its actual PID for this run was `88749`.

```text
rtk proxy jcmd 88749 Thread.print
```

Selected output captured on 2026-08-28:

```text
"jcmd-observation-worker" #25 [...]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
    at java.lang.Thread.sleepNanos0(java.base@25.0.2/Native Method)
    at java.lang.Thread.sleep(java.base@25.0.2/Thread.java:540)
    at org.example.probes.ThreadObservationProgram.waitForRelease(ThreadObservationProgram.java:24)
    at org.example.probes.ThreadObservationProgram.lambda$main$0(ThreadObservationProgram.java:13)
    at java.lang.Thread.run(java.base@25.0.2/Thread.java:1474)
```

This establishes that, at the instant `jcmd` captured the dump, the inspected JVM had a thread with that name whose observed Java stack was blocked in the probe's `sleep` call. It does not establish a permanent state, physical stack/heap placement, every object reachable from that thread, or behavior before/after the snapshot. The probe process was terminated immediately after capture.

`jcmd` sends diagnostic commands to a running JVM selected by process ID and normally requires the same machine and effective user/group identity as the target process, per the [JDK 21 `jcmd` documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html).
