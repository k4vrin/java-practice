package org.example.probes;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NumericAndResourceProbeTest {
    private static final class FakeResource implements AutoCloseable {
        String name;
        List<String> closeOrder;
        RuntimeException closeFailure;

        private FakeResource(
                String name,
                List<String> closeOrder,
                RuntimeException closeFailure
        ) {
            this.name = name;
            this.closeOrder = closeOrder;
            this.closeFailure = closeFailure;
        }

        @Override
        public void close() {
            closeOrder.add(name);
            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }

    private static FakeResource acquireFailing(
            RuntimeException acquisitionFailure
    ) {
        throw acquisitionFailure;
    }

    @Test
    void doubleArithmeticProbe() {
        double result = 0.1 + 0.2;

        assertNotEquals(0.3, result);
    }

    @Test
    void bigDecimalArithmeticProbe() {
        BigDecimal a = new BigDecimal("0.1");
        BigDecimal b = new BigDecimal("0.2");

        BigDecimal result = a.add(b);

        assertEquals(new BigDecimal("0.3"), result);
    }

    @Test
    void bigDecimalDoubleConstructorProbe() {
        BigDecimal fromDouble = new BigDecimal(0.1);
        BigDecimal fromString = new BigDecimal("0.1");

        assertNotEquals(fromString, fromDouble);
    }

    @Test
    void bigDecimalEqualsVsCompareToProbe() {
        BigDecimal a = new BigDecimal("1.0");
        BigDecimal b = new BigDecimal("1.00");

        assertNotEquals(a, b);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void integerCacheProbeFor100() {
        Integer a = Integer.valueOf(100);
        Integer b = Integer.valueOf(100);

        assertSame(a, b);
        assertEquals(a, b);
    }

    @Test
    void integerCacheProbeFor1000() {
        Integer a = Integer.valueOf(1000);
        Integer b = Integer.valueOf(1000);

        // Guaranteed: both represent the same numeric value.
        assertEquals(a, b);

        // Observation only: a == b may be true or false depending on
        // whether this JVM caches Integer 1000.
        boolean sameObjectOnThisJvm = a == b;
        System.out.println("Integer.valueOf(1000) reused object: " + sameObjectOnThisJvm);
    }

    @Test
    void unboxingNullIntegerThrows() {
        Integer boxed = null;

        assertThrows(NullPointerException.class, () -> {
            int value = boxed;
        });
    }

    @Test
    void bodyAndBothClosesSucceed() {
        List<String> closeOrder = new ArrayList<>();

        try (
                FakeResource a = new FakeResource("A", closeOrder, null);
                FakeResource b = new FakeResource("B", closeOrder, null)
        ) {
            // body succeeds
        }

        assertEquals(List.of("B", "A"), closeOrder);
    }

    @Test
    void bodyFailsAndBothClosesSucceed() {
        RuntimeException bodyFailure = new RuntimeException("body");
        List<String> closeOrder = new ArrayList<>();

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (
                    FakeResource a = new FakeResource("A", closeOrder, null);
                    FakeResource b = new FakeResource("B", closeOrder, null)
            ) {
                throw bodyFailure;
            }
        });

        assertEquals(List.of("B", "A"), closeOrder);
        assertSame(bodyFailure, thrown);
        assertEquals(0, thrown.getSuppressed().length);
    }

    @Test
    void bodyAndBothClosesFail() {
        // setup
        RuntimeException bodyFailure = new RuntimeException("body");
        RuntimeException aCloseFailure = new RuntimeException("close A");
        RuntimeException bCloseFailure = new RuntimeException("close B");
        List<String> closeOrder = new ArrayList<>();

        // execute try-with-resources
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (
                    FakeResource a = new FakeResource("A", closeOrder, aCloseFailure);
                    FakeResource b = new FakeResource("B", closeOrder, bCloseFailure)
            ) {
                throw bodyFailure;
            }
        });

        // assertions
        // B closes first, then A
        assertEquals(List.of("B", "A"), closeOrder);

        // The body exception remains the primary exception
        assertSame(bodyFailure, thrown);

        // Both close exceptions are suppressed, in close order
        assertArrayEquals(
                new Throwable[]{bCloseFailure, aCloseFailure},
                thrown.getSuppressed()
        );
    }

    @Test
    void bodySucceedsAndBothClosesFail() {
        RuntimeException aCloseFailure = new RuntimeException("close A");
        RuntimeException bCloseFailure = new RuntimeException("close B");
        List<String> closeOrder = new ArrayList<>();

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (
                    FakeResource a = new FakeResource("A", closeOrder, aCloseFailure);
                    FakeResource b = new FakeResource("B", closeOrder, bCloseFailure)
            ) {
                // body succeeds
            }
        });

        assertEquals(List.of("B", "A"), closeOrder);

        assertSame(bCloseFailure, thrown);

        assertArrayEquals(
                new Throwable[]{aCloseFailure},
                thrown.getSuppressed()
        );
    }

    @Test
    void acquiringBFailsAfterAWasAcquired() {
        List<String> closeOrder = new ArrayList<>();

        RuntimeException acquisitionFailure =
                new RuntimeException("acquire B");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (
                    FakeResource a =
                            new FakeResource("A", closeOrder, null);

                    FakeResource b =
                            acquireFailing(acquisitionFailure)
            ) {
                fail("body must not execute");
            }
        });

        assertSame(acquisitionFailure, thrown);
        assertEquals(List.of("A"), closeOrder);
        assertEquals(0, thrown.getSuppressed().length);
    }
}
