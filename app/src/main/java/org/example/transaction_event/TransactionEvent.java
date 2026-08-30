package org.example.transaction_event;

import java.util.Objects;

public record TransactionEvent(
        String accountId,
        String merchantId,
        long amountCents
) {}