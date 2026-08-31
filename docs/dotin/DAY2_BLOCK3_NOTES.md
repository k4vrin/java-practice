# Day 2 Block 3 — Attempt Notes

## Before editing

1. Shared state in `RequestCounter`:

2. One failing counter interleaving:

3. Why `ConcurrentHashMap` does not make the current frequency update atomic:

4. Baseline test result, including expected and actual values:

## After repair

5. Mechanism used for the counter and its correctness argument:

6. Mechanism used for frequency updates and its correctness argument:

7. Why can an unbounded executor queue be dangerous in a backend service? What does a bounded queue require you to decide?

8. A task submitted with `submit()` throws an exception. Where does the exception go, and how will the application observe it?

9. What does `Future.cancel(true)` guarantee, and what must the task do for cancellation to work?

10. Give a bounded, production-safe executor shutdown sequence. What should happen if the waiting thread is interrupted?

