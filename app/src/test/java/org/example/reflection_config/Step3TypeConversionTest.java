package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.example.reflection_config.ReflectionConfigFixtures.BooleanConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.IntegerConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.Mode;
import static org.example.reflection_config.ReflectionConfigFixtures.ScalarConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.UnsupportedTypeConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step3TypeConversionTest {
    private final ReflectiveConfigBinder binder = new ReflectiveConfigBinder();

    @Test
    void convertsEverySupportedScalarAndDistinguishesPrimitiveFromBoxedTypes() {
        ScalarConfig config = binder.bind(ScalarConfig.class, Map.of(
                "primitive.int", "41",
                "boxed.int", "42",
                "primitive.long", "9000000000",
                "boxed.long", "9000000001",
                "primitive.boolean", "true",
                "boxed.boolean", "FALSE",
                "mode", "PRODUCTION"
        ));

        assertEquals(41, config.primitiveInt());
        assertEquals(42, config.boxedInt());
        assertEquals(9_000_000_000L, config.primitiveLong());
        assertEquals(9_000_000_001L, config.boxedLong());
        assertTrue(config.primitiveBoolean());
        assertFalse(config.boxedBoolean());
        assertEquals(Mode.PRODUCTION, config.mode());
    }

    @Test
    void rejectsInvalidBooleanInsteadOfSilentlyTreatingItAsFalse() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(BooleanConfig.class, Map.of("feature.enabled", "yes"))
        );

        assertTrue(error.getMessage().contains("feature.enabled"));
    }

    @Test
    void wrapsNumberConversionFailureAndNamesItsProperty() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(IntegerConfig.class, Map.of("workers", "many"))
        );

        assertTrue(error.getMessage().contains("workers"));
        assertTrue(error.getCause() instanceof NumberFormatException);
    }

    @Test
    void rejectsUnsupportedFieldTypeAndNamesTheType() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(UnsupportedTypeConfig.class, Map.of("timeout", "PT2S"))
        );

        assertTrue(error.getMessage().contains(Duration.class.getName()));
    }
}
