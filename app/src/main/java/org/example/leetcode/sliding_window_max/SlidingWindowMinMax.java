package org.example.leetcode.sliding_window_max;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowMinMax {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int[] result = new int[nums.length - k + 1];
        for (int i = 0; i < nums.length - k + 1; i++) {
            int max = Integer.MIN_VALUE;
            for (int j = i; j < i + k; j++) {
                max = Math.max(max, nums[j]);
            }
            result[i] = max;
        }

        return result;
    }

    public int[] maxSlidingWindowDeque(int[] nums, int k) {
        Deque<Integer> deque = new ArrayDeque<>();
        int[] result = new int[nums.length - k + 1];

        for (int i = 0; i < nums.length; i++) {
            int leftBoundary = i - k + 1;
            // Remove indices that are out of the current window
            while (!deque.isEmpty() && deque.peekFirst() < leftBoundary) {
                deque.pollFirst();
            }
            // Remove indices whose corresponding values are less than new element nums[i]
            // to keep the deque in decreasing order
            while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
                deque.pollLast();
            }
            deque.addLast(i);

            if (leftBoundary >= 0) {
                var maxIndex = deque.peekFirst();
                result[leftBoundary] = nums[maxIndex];
            }
        }

        return result;
    }

    public int[] maxSlidingWindowDequeWithStep(int[] nums, int k, int step) {
        Deque<Integer> deque = new ArrayDeque<>();
        int[] result = new int[((nums.length - k) / step )+ 1];

        for (int i = 0; i < nums.length; i++) {
            int leftBoundary = i - k + 1;
            // Remove indices that are out of the current window
            while (!deque.isEmpty() && deque.peekFirst() < leftBoundary) {
                deque.pollFirst();
            }
            // Remove indices whose corresponding values are less than new element nums[i]
            // to keep the deque in decreasing order
            while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
                deque.pollLast();
            }
            deque.addLast(i);

            if (leftBoundary >= 0 && leftBoundary % step == 0) {
                var maxIndex = deque.peekFirst();
                result[(leftBoundary) / step] = nums[maxIndex];
            }
        }

        return result;
    }

    public int[] minSlidingWindowDeque(int[] nums, int k) {
        Deque<Integer> deque = new ArrayDeque<>();
        int[] result = new int[nums.length - k + 1];

        for (int i = 0; i < nums.length; i++) {
            int leftBoundary = i - k + 1;
            // Remove indices that are out of the current window
            while (!deque.isEmpty() && deque.peekFirst() < leftBoundary) {
                deque.pollFirst();
            }

            while (!deque.isEmpty() && nums[deque.peekLast()] > nums[i]) {
                deque.pollLast();
            }
            deque.addLast(i);

            if (leftBoundary >= 0) {
                var minIndex = deque.peekFirst();
                result[leftBoundary] = nums[minIndex];
            }
        }

        return result;
    }
}
