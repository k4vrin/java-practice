package org.example.transaction_event;

import java.util.*;

public final class NearbyDuplicateFinder {

    public static Optional<IndexPair> findNearbyDuplicate(
            List<TransactionEvent> events,
            int k) {

        if (events == null) {
            throw new IllegalArgumentException("events must not be null");
        }



        for (TransactionEvent event: events) {
            if (event == null) {
                throw new IllegalArgumentException("event must not be null");
            }

            if (event.accountId() == null) {
                throw new IllegalArgumentException("accountId must not be null");
            }

            if (event.merchantId() == null) {
                throw new IllegalArgumentException("merchantId must not be null");
            }
        }

        if (k <= 0) {
            return Optional.empty();
        }

        Map<EventIdentity, Integer> lastSeen = new HashMap<>();

        for (int j = 0; j < events.size(); j++) {
            TransactionEvent event = events.get(j);

            EventIdentity identity =
                    new EventIdentity(event.accountId(), event.merchantId());

            Integer i = lastSeen.get(identity);

            if (i != null && j - i <= k) {
                return Optional.of(new IndexPair(i, j));
            }

            lastSeen.put(identity, j);
        }

        return Optional.empty();
    }

    public static EventIdentity toEventId(TransactionEvent event) {
        return new EventIdentity(event.accountId(), event.merchantId());
    }
}
