# Day 2 Block 4 — Answers

1. TRUE. place order gets called from OrderController so the operation goes through the Spring proxy and is transactionally.

2. FALSE. calling write audit from wihtin paceOrder is self-invocation and does not go through the Spring proxy to create another independent transaction

3. TRUE. Transactionaly means it wil adhere to atomicity. al or nothiing

4. FALSE. Local database transactions can not undo a successful call to another service. Remote effects requiere a seperate system design ike compensation, outbox.

5. FALSE. the markFailed method is expicitly cathing RuntimeException. so it will not rollback

6. FALSE. flush does run the sql and expose any contraint erros but it is not durable and will rollback on failure.

7. TRUE. we can use fetch join or entity graph or other techniques to prevent that.

8. FALSE. Fetching a collection with pagination needs care because row multiplication can break efficient or correct page behavior

9. FALSE. Use a database UNIQUE constraint for the idempotency scope and handle the resulting conflict within a clear transaction/API contract

10. FALSE. We should choose a a database concurrency mechanism matching the invariant:

## Final design response


