package org.example.interview_week.day1.customer_directory;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerDirectoryTest {

    @Test
    void retrievesTagsUsingAnEquivalentCustomerId() {
        CustomerDirectory directory = new CustomerDirectory();
        directory.register(new CustomerId(new byte[]{1, 2, 3}), List.of("priority"));

        List<String> tags = directory
                .findTags(new CustomerId(new byte[]{1, 2, 3}))
                .orElseThrow();

        assertEquals(List.of("priority"), tags);
    }

    @Test
    void changingTheConstructorArrayDoesNotCorruptTheHashKey() {
        byte[] source = {1, 2, 3};
        CustomerId customerId = new CustomerId(source);
        CustomerDirectory directory = new CustomerDirectory();
        directory.register(customerId, List.of("priority"));

        source[0] = 9;

        assertEquals(
                List.of("priority"),
                directory.findTags(new CustomerId(new byte[]{1, 2, 3})).orElseThrow()
        );
    }

    @Test
    void changingTheInputTagsDoesNotChangeStoredState() {
        List<String> sourceTags = new ArrayList<>(List.of("priority"));
        CustomerDirectory directory = new CustomerDirectory();
        CustomerId customerId = new CustomerId(new byte[]{1});
        directory.register(customerId, sourceTags);

        sourceTags.add("mutated");

        assertEquals(List.of("priority"), directory.findTags(customerId).orElseThrow());
    }

    @Test
    void returnedTagsCannotMutateStoredState() {
        CustomerDirectory directory = new CustomerDirectory();
        CustomerId customerId = new CustomerId(new byte[]{1});
        directory.register(customerId, new ArrayList<>(List.of("priority")));

        List<String> returnedTags = directory.findTags(customerId).orElseThrow();

        assertThrows(UnsupportedOperationException.class, () -> returnedTags.add("mutated"));
        assertEquals(List.of("priority"), directory.findTags(customerId).orElseThrow());
    }

    @Test
    void missingCustomerReturnsAnEmptyOptional() {
        CustomerDirectory directory = new CustomerDirectory();
        assertFalse(directory.findTags(new CustomerId(new byte[]{9})).isPresent());
    }
}
