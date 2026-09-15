package org.example.interview_week.day1.customer_directory;

import java.util.Arrays;

/**
 * Starter value object for the A-W1-D1 changed code-review exercise.
 *
 * <p>The implementation compiles, but its boundary behavior must be reviewed
 * against the tests before it can be considered safe.</p>
 */
public final class CustomerId {
    private final byte[] value;

    public CustomerId(byte[] value) {
        this.value = value.clone();
    }

    public byte[] value() {
        return value.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomerId that)) {
            return false;
        }
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
