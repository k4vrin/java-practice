package org.example.interview_week.day1.customer_directory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Starter implementation for review and repair. */
public final class CustomerDirectory {
    private final Map<CustomerId, List<String>> tagsByCustomer = new HashMap<>();

    public void register(CustomerId customerId, List<String> tags) {
        tagsByCustomer.put(customerId, List.copyOf(tags));
    }

    public Optional<List<String>> findTags(CustomerId customerId) {
        return Optional.ofNullable(tagsByCustomer.get(customerId))
                .map(List::copyOf);
    }
}
