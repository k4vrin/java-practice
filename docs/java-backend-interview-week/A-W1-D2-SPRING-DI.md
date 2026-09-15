# A-W1-D2 — Spring dependency injection starter

## Reading

[Spring Framework: Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)

Read the definition of dependency injection, constructor-based injection, and
the constructor-versus-setter guidance. Complete the interactive reading task
before opening a worked Spring configuration.

## Existing behavior

- `ReservationService` declares two required collaborators through its constructor.
- `ReservationServiceTest` constructs the service directly with small test doubles;
  it should not need a Spring context.
- `DependencyInjectionContextTest` asks Spring for the service and both collaborators.
  It is expected to fail initially because the starter makes no bean-registration
  decision for you.

## Your task

1. Run the plain unit test and the context test separately.
2. Explain why one can pass while the other fails.
3. Choose either component scanning or explicit `@Bean` configuration.
4. Register the service and both adapters without adding field injection or a
   service locator.
5. Rerun both tests and record the commands and outcomes.
6. Defend whether framework annotations belong on the application service and
   infrastructure adapters in this small exercise.

Do not add the controller, validation, error, security, or transaction layers yet.
They are added after their corresponding interactive reading tasks.
