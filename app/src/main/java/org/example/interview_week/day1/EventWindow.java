package org.example.interview_week.day1;

import java.util.HashMap;
import java.util.Map;

/**
 * Day 1 coding diagnostic for the Java Backend Interview Week.
 *
 * <p>Commit the contract, baseline, invariant, complexity target, and test cases
 * before replacing the starter implementation.</p>
 */
public final class EventWindow {

    private EventWindow() {
    }

    /**
     * Returns the length of the longest contiguous subarray containing at most
     * {@code k} distinct values.
     *
     * @param eventCodes input values
     * @param k maximum number of distinct values allowed in the subarray
     * @return the longest valid contiguous-subarray length
     */
    public static int longestAtMostKDistinct(int[] eventCodes, int k) {
        int left = 0;
        int best = 0;

        Map<Integer, Integer> distinctMap = new HashMap<>();

        for (int right = 0; right < eventCodes.length; right++) {
            distinctMap.put(eventCodes[right], distinctMap.getOrDefault(eventCodes[right], 0) + 1);

            while (distinctMap.size() > k) {

                distinctMap.put(eventCodes[left], distinctMap.get(eventCodes[left]) - 1);
                if (distinctMap.get(eventCodes[left]) == 0) {
                    distinctMap.remove(eventCodes[left]);
                }
                left++;
            }

            best = Math.max(best, right - left + 1);
        }

        return best;
    }
}
