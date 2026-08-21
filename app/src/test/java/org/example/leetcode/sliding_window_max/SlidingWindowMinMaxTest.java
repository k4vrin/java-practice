package org.example.leetcode.sliding_window_max;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowMinMaxTest {

    private static SlidingWindowMinMax slidingWindowMinMax;

    @BeforeAll
    static void setup() {
        slidingWindowMinMax = new SlidingWindowMinMax();
        System.out.println("Running SlidingWindowMinMaxTest...");
    }

    @Test
    void maxSlidingWindow() {
        int[] nums = {1,3,-1,-3,5,3,6,7};
        int k = 3;
        int[] expected = {3,3,5,5,6,7};
        assertArrayEquals(expected, slidingWindowMinMax.maxSlidingWindowDeque(nums, k));
    }

    @Test
    void maxSlidingWindow_stepOne() {
        int[] nums = {1,3,-1,-3,5,3,6,7};
        int k = 3;
        int[] expected = {3,3,5,5,6,7};
        assertArrayEquals(expected, slidingWindowMinMax.maxSlidingWindowDequeWithStep(nums, k, 1));
    }

    @Test
    void maxSlidingWindow_stepTwo() {

        int[] nums = {4, 2, 7, 3, 8, 1, 6, 9, 2};
        int k = 3;
        int step = 2;

        int[] expected = {7, 8, 8, 9};

        assertArrayEquals(
                expected,
                slidingWindowMinMax.maxSlidingWindowDequeWithStep(nums, k, step)
        );
    }

    @Test
    void minSlidingWindow_mixedValues() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;

        int[] expected = {-1, -3, -3, -3, 3, 3};

        assertArrayEquals(
                expected,
                slidingWindowMinMax.minSlidingWindowDeque(nums, k)
        );
    }

    @Test
    void minSlidingWindow_increasingValues() {
        int[] nums = {1, 2, 3, 4, 5, 6};
        int k = 3;

        int[] expected = {1, 2, 3, 4};

        assertArrayEquals(
                expected,
                slidingWindowMinMax.minSlidingWindowDeque(nums, k)
        );
    }

}