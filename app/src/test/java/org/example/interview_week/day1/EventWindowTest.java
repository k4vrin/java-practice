package org.example.interview_week.day1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventWindowTest {

    @Test
    void findsLongestWindowWithAtMostTwoDistinctValues() {
        int[] eventCodes = {1, 2, 1, 2, 3};

        int result = EventWindow.longestAtMostKDistinct(eventCodes, 2);

        assertEquals(4, result);
    }

    @Test
    void acceptsTheWholeInputWhenItContainsAtMostKDistinctValues() {
        int[] eventCodes = {4, 4, 7, 4};

        int result = EventWindow.longestAtMostKDistinct(eventCodes, 2);

        assertEquals(4, result);
    }

    @Test
    void handlesRepeatedShrinkingAfterNewDistinctValuesArrive() {
        int[] eventCodes = {1, 2, 1, 3, 4, 3, 5};

        int result = EventWindow.longestAtMostKDistinct(eventCodes, 2);

        assertEquals(3, result);
    }

    @Test
    void returnsOneWhenOnlyOneDistinctValueIsAllowed() {
        int[] eventCodes = {1, 2, 3, 4};

        int result = EventWindow.longestAtMostKDistinct(eventCodes, 1);

        assertEquals(1, result);
    }

    @Test
    void returnsZeroForEmptyInput() {
        int result = EventWindow.longestAtMostKDistinct(new int[0], 2);

        assertEquals(0, result);
    }

    @Test
    void returnsZeroWhenKIsZero() {
        int[] eventCodes = {1, 1, 2};

        int result = EventWindow.longestAtMostKDistinct(eventCodes, 0);

        assertEquals(0, result);
    }

    @Test
    void acceptsTheWholeInputWhenKExceedsTheDistinctCount() {
        int[] eventCodes = {5, 6, 7};

        int result = EventWindow.longestAtMostKDistinct(eventCodes, 10);

        assertEquals(3, result);
    }
}
