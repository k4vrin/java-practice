package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.example.reflection_config.ReflectionConfigFixtures.ChildConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.DuplicateChildConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.FinalFieldConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.StaticFieldConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step4HierarchyAndValidationTest {
    private final ReflectiveConfigBinder binder = new ReflectiveConfigBinder();

    @Test
    void scansAnnotatedFieldsDeclaredBySuperclasses() {
        ChildConfig config = binder.bind(ChildConfig.class, Map.of(
                "service.name", "billing",
                "service.retries", "3"
        ));

        assertEquals("billing", config.name());
        assertEquals(3, config.retries());
    }

    @Test
    void rejectsAnnotatedStaticFieldsWithoutMutatingGlobalState() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(StaticFieldConfig.class, Map.of("global", "changed"))
        );

        assertTrue(error.getMessage().toLowerCase().contains("static"));
        assertEquals("original", StaticFieldConfig.global);
    }

    @Test
    void rejectsAnnotatedFinalFields() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(FinalFieldConfig.class, Map.of("name", "changed"))
        );

        assertTrue(error.getMessage().toLowerCase().contains("final"));
    }

    @Test
    void rejectsDuplicatePropertyKeysAcrossTheClassHierarchy() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(DuplicateChildConfig.class, Map.of("shared", "value"))
        );

        assertTrue(error.getMessage().contains("shared"));
    }
}
