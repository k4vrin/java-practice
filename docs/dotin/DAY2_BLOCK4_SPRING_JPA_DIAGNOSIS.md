# Day 2 Block 4 — Spring/JPA/REST Diagnosis

## Conditions

- Time: 50 minutes
- Closed book after the prescribed primer: no internet, AI, or notes
- This is diagnosis, not implementation. Do not create a Spring project.
- Write answers in `DAY2_BLOCK4_ANSWERS.md`.

## Scenario

An order endpoint intermittently creates duplicate orders, audit records do not behave independently as expected, some failures leave confusing external effects, and the list endpoint produces excessive SQL.

Assume all shown classes are Spring-managed where their annotations indicate, default proxy-based transaction management is used, and no unshown database uniqueness or version constraint exists.

## Supplied code

```java
@RestController
final class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(201).body(OrderResponse.from(order));
    }

    @GetMapping("/orders")
    List<OrderSummary> recent() {
        return orderService.findRecent().stream()
                .map(order -> new OrderSummary(order.getId(), order.getLines().size()))
                .toList();
    }
}
```

```java
@Service
final class OrderService {
    private final OrderRepository orders;
    private final AuditRepository audits;
    private final InventoryClient inventoryClient;

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        if (orders.existsByRequestId(request.requestId())) {
            return orders.findByRequestId(request.requestId()).orElseThrow();
        }

        Order order = orders.save(Order.from(request));
        writeAudit(order.getId());
        inventoryClient.reserve(request.productId(), request.quantity());
        return order;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void writeAudit(long orderId) {
        audits.save(new AuditRecord(orderId, "CREATED"));
    }

    @Transactional
    public void markFailed(long orderId) {
        try {
            Order order = orders.findById(orderId).orElseThrow();
            order.markFailed();
            orders.flush();
            inventoryClient.release(order.getProductId(), order.getQuantity());
        } catch (RuntimeException failure) {
            log.warn("Could not mark order failed", failure);
        }
    }

    @Transactional(readOnly = true)
    public List<Order> findRecent() {
        return orders.findTop100ByOrderByCreatedAtDesc();
    }
}
```

```java
@Entity
final class Order {
    @Id
    private Long id;

    private String requestId;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderLine> lines;
}

interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByRequestId(String requestId);
    Optional<Order> findByRequestId(String requestId);
    List<Order> findTop100ByOrderByCreatedAtDesc();
}
```

## Required diagnosis

For each statement, write **TRUE** or **FALSE**, then justify it in at most three sentences. For every false statement, give the corrected rule or boundary.

1. The controller's external call to `placeOrder` crosses the service proxy, so the transaction normally begins before the method body executes.
2. The same-object call to `writeAudit` activates `REQUIRES_NEW`, so the audit commits independently of `placeOrder`.
3. If an unchecked exception escapes `placeOrder`, Spring's default transaction rules normally roll back its database work.
4. If `inventoryClient.reserve(...)` succeeds and the database transaction later rolls back, the local transaction automatically undoes the remote reservation.
5. Because `markFailed` catches `RuntimeException` and returns normally, Spring is guaranteed to roll back the transaction.
6. `orders.flush()` commits the order status permanently before the inventory release call.
7. The list endpoint can produce an N+1 query pattern when mapping `order.getLines().size()`; generated SQL or query counts should be inspected before claiming it.
8. Changing `findRecent` to one collection fetch-join query with pagination is always a safe and efficient N+1 fix.
9. `existsByRequestId` followed by `save` prevents duplicate request IDs across concurrent requests and multiple application instances.
10. `@Transactional` alone prevents two concurrent modifications of the same order from causing a lost update.

## Final design response

In no more than eight sentences, propose the smallest responsible repair covering:

- independent audit transaction behavior;
- idempotency enforcement and replay/conflict handling;
- local transaction versus inventory-service effects;
- measured N+1 repair.

## Scoring — 10 points

- Statements 1–10: 0.75 each = 7.5
- Final design response: 2
- Concision and precise terminology: 0.5

Target: **8/10**. Critical misses include claiming self-invocation activates `REQUIRES_NEW`, flush equals commit, a local transaction rolls back HTTP effects, or `@Transactional` alone prevents lost updates.

