package org.example.transaction_event;

public record EventIdentity(
        String accountId,
        String merchantId) {
}