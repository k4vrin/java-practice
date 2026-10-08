package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.example.reflection_config.ReflectionConfigFixtures.NoDefaultConstructorConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.PrivateConstructorConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.ThrowingConstructorConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step1ConstructionTest {
    private final ReflectiveConfigBinder binder = new ReflectiveConfigBinder();

    @Test
    void rejectsNullTargetTypeBeforeUsingReflection() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> binder.bind(null, Map.of())
        );

        assertTrue(error.getMessage().toLowerCase().contains("type"));
    }

    @Test
    void usesPrivateNoArgConstructor() {
        PrivateConstructorConfig config = binder.bind(PrivateConstructorConfig.class, Map.of());

        assertEquals("constructed", config.state());
    }

    @Test
    void rejectsTypeWithoutNoArgConstructor() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(NoDefaultConstructorConfig.class, Map.of())
        );

        assertTrue(error.getMessage().toLowerCase().contains("no-arg"));
    }

    @Test
    void exposesConstructorFailureAsTheCauseRatherThanInvocationTargetException() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(ThrowingConstructorConfig.class, Map.of())
        );

        assertInstanceOf(IllegalArgumentException.class, error.getCause());
        assertFalse(error.getCause() instanceof InvocationTargetException);
        assertEquals("construction rejected", error.getCause().getMessage());
    }
}
