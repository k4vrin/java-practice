package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.example.reflection_config.ReflectionConfigFixtures.BasicConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.OptionalConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step2FieldBindingTest {
    private final ReflectiveConfigBinder binder = new ReflectiveConfigBinder();

    @Test
    void rejectsNullPropertyMapBeforeBindingFields() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> binder.bind(BasicConfig.class, null)
        );

        assertTrue(error.getMessage().toLowerCase().contains("properties"));
    }

    @Test
    void bindsStringToPrivateAnnotatedField() {
        BasicConfig config = binder.bind(
                BasicConfig.class,
                Map.of("service.name", "billing")
        );

        assertEquals("billing", config.name());
    }

    @Test
    void leavesOptionalPropertyAtItsDeclaredDefaultWhenAbsent() {
        OptionalConfig config = binder.bind(OptionalConfig.class, Map.of());

        assertEquals("localhost", config.host());
    }

    @Test
    void rejectsMissingRequiredPropertyAndNamesItsKey() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(BasicConfig.class, Map.of())
        );

        assertTrue(error.getMessage().contains("service.name"));
    }
}
