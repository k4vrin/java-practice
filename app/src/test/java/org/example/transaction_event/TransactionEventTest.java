package org.example.transaction_event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionEventTest {

    @Test
    void emptyListShouldReturnEmpty() {
        var result = NearbyDuplicateFinder.findNearbyDuplicate(
                List.of(),
                2
        );

        assertEquals(Optional.empty(), result);
    }

    @Test
    void kZeroShouldReturnEmpty() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("A", "M1", 200)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 0);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void negativeKShouldReturnEmpty() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("A", "M1", 200)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, -1);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void noRepeatedIdentityShouldReturnEmpty() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("B", "M1", 200),
                new TransactionEvent("A", "M2", 300)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 3);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void matchExactlyAtDistanceKShouldReturnPair() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("B", "M1", 200),
                new TransactionEvent("A", "M1", 300)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 2);

        assertEquals(Optional.of(new IndexPair(0, 2)), result);
    }

    @Test
    void matchJustOutsideKShouldReturnEmpty() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("B", "M1", 200),
                new TransactionEvent("A", "M1", 300)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 1);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void sameAccountDifferentMerchantShouldNotMatch() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("A", "M2", 100)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 1);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void sameMerchantDifferentAccountShouldNotMatch() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("B", "M1", 100)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 1);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void sameIdentityDifferentAmountsShouldMatch() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("B", "M2", 500),
                new TransactionEvent("A", "M1", 999)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 2);

        assertEquals(Optional.of(new IndexPair(0, 2)), result);
    }

    @Test
    void moreThanTwoOccurrencesShouldUseEarliestValidSecondIndex() {
        var events = List.of(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("B", "M2", 200),
                new TransactionEvent("A", "M1", 300),
                new TransactionEvent("A", "M1", 400)
        );

        var result = NearbyDuplicateFinder.findNearbyDuplicate(events, 3);

        // (0,2) wins over (2,3) because j=2 is smaller.
        assertEquals(Optional.of(new IndexPair(0, 2)), result);
    }

    @Test
    void nullListShouldThrowIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> NearbyDuplicateFinder.findNearbyDuplicate(null, 2)
        );
    }

    @Test
    void nullEventShouldThrowIllegalArgumentException() {
        List<TransactionEvent> events = new ArrayList<>();
        events.add(new TransactionEvent("A", "M1", 100));
        events.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> NearbyDuplicateFinder.findNearbyDuplicate(events, 2)
        );
    }

    @Test
    void nullAccountShouldThrowIllegalArgumentException() {
        var events = List.of(
                new TransactionEvent(null, "M1", 100)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> NearbyDuplicateFinder.findNearbyDuplicate(events, 2)
        );
    }

    @Test
    void nullMerchantShouldThrowIllegalArgumentException() {
        var events = List.of(
                new TransactionEvent("A", null, 100)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> NearbyDuplicateFinder.findNearbyDuplicate(events, 2)
        );
    }

    @Test
    void inputShouldRemainUnchanged() {
        var event1 = new TransactionEvent("A", "M1", 100);
        var event2 = new TransactionEvent("B", "M2", 200);
        var event3 = new TransactionEvent("A", "M1", 300);

        List<TransactionEvent> events =
                new ArrayList<>(List.of(event1, event2, event3));

        List<TransactionEvent> original =
                new ArrayList<>(events);

        NearbyDuplicateFinder.findNearbyDuplicate(events, 2);

        assertEquals(original, events);
    }

    @Test
    void rejectsNullEventEvenWhenEarlierValidMatchExists() {
        List<TransactionEvent> events = Arrays.asList(
                new TransactionEvent("A", "M1", 100),
                new TransactionEvent("A", "M1", 200),
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> NearbyDuplicateFinder.findNearbyDuplicate(events, 1)
        );
    }
}