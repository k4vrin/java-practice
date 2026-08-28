package org.example.leetcode.reverse_integer;

/** Solutions for LeetCode 7 that detect overflow without widening to long. */
public final class ReverseInteger {

    private ReverseInteger() {
    }

    /**
     * Brute-force approach: reverse the digit characters, then let {@link Integer#parseInt(String)}
     * validate whether the signed result fits in an int.
     *
     * Time: O(d). Auxiliary space: O(d), where d is the number of decimal digits.
     */
    public static int reverseBruteForce(int x) {
        String value = Integer.toString(x);
        boolean isNegative = value.charAt(0) == '-';
        String digits = isNegative ? value.substring(1) : value;
        String reversedDigits = new StringBuilder(digits).reverse().toString();
        String signedResult = isNegative ? "-" + reversedDigits : reversedDigits;

        try {
            return Integer.parseInt(signedResult);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * Optimal approach: before appending a digit, check whether multiplying the current result by
     * ten and adding that digit would exceed the int range.
     *
     * Time: O(d). Auxiliary space: O(1), where d is the number of decimal digits.
     */
    public static int reverse(int x) {
        int reversed = 0;

        while (x != 0) {
            int digit = x % 10;
            x /= 10;

            if (reversed > Integer.MAX_VALUE / 10
                    || (reversed == Integer.MAX_VALUE / 10 && digit > 7)) {
                return 0;
            }
            if (reversed < Integer.MIN_VALUE / 10
                    || (reversed == Integer.MIN_VALUE / 10 && digit < -8)) {
                return 0;
            }

            reversed = reversed * 10 + digit;
        }

        return reversed;
    }
}
