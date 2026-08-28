package org.example.leetcode.reverse_integer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReverseIntegerTest {

    @Test
    void reversesPositiveInput() {
        assertBothApproaches(123, 321);
    }

    @Test
    void reversesNegativeInput() {
        assertBothApproaches(-123, -321);
    }

    @Test
    void preservesZero() {
        assertBothApproaches(0, 0);
    }

    @Test
    void preservesSingleDigits() {
        assertBothApproaches(7, 7);
        assertBothApproaches(-8, -8);
    }

    @Test
    void removesTrailingZerosAfterReversal() {
        assertBothApproaches(120, 21);
        assertBothApproaches(-120, -21);
    }

    @Test
    void reversesValuesNearBothIntegerBoundariesWhenTheResultFits() {
        assertBothApproaches(1_463_847_412, 2_147_483_641);
        assertBothApproaches(-1_463_847_412, -2_147_483_641);
    }

    @Test
    void returnsZeroForPositiveOverflow() {
        assertBothApproaches(1_534_236_469, 0);
    }

    @Test
    void returnsZeroForNegativeOverflow() {
        assertBothApproaches(-1_563_847_412, 0);
    }

    @Test
    void returnsZeroForIntegerMaxValue() {
        assertBothApproaches(Integer.MAX_VALUE, 0);
    }

    @Test
    void returnsZeroForIntegerMinValue() {
        assertBothApproaches(Integer.MIN_VALUE, 0);
    }

    private static void assertBothApproaches(int input, int expected) {
        assertEquals(expected, ReverseInteger.reverseBruteForce(input));
        assertEquals(expected, ReverseInteger.reverse(input));
    }
}
