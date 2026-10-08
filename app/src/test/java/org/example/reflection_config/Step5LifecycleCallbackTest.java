package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.example.reflection_config.ReflectionConfigFixtures.InheritedCallbackConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.InvalidCallbackConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.MultipleCallbacksConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.ThrowingCallbackConfig;
import static org.example.reflection_config.ReflectionConfigFixtures.ValidatedConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step5LifecycleCallbackTest {
    private final ReflectiveConfigBinder binder = new ReflectiveConfigBinder();

    @Test
    void invokesPrivateAfterBindingMethodAfterAllFieldsArePopulated() {
        ValidatedConfig config = binder.bind(ValidatedConfig.class, Map.of(
                "min", "4",
                "max", "9"
        ));

        assertTrue(config.validated());
        assertEquals(5, config.range());
    }

    @Test
    void findsAfterBindingMethodDeclaredBySuperclass() {
        InheritedCallbackConfig config = binder.bind(
                InheritedCallbackConfig.class,
                Map.of("label", "ready")
        );

        assertTrue(config.callbackInvoked());
    }

    @Test
    void rejectsMultipleAfterBindingMethodsAcrossHierarchy() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(MultipleCallbacksConfig.class, Map.of())
        );

        assertTrue(error.getMessage().contains(AfterBinding.class.getSimpleName()));
    }

    @Test
    void rejectsAfterBindingMethodWithParameters() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(InvalidCallbackConfig.class, Map.of())
        );

        assertTrue(error.getMessage().toLowerCase().contains("parameter"));
    }

    @Test
    void exposesCallbackFailureAsTheCauseRatherThanInvocationTargetException() {
        ConfigurationException error = assertThrows(
                ConfigurationException.class,
                () -> binder.bind(ThrowingCallbackConfig.class, Map.of())
        );

        assertInstanceOf(IllegalStateException.class, error.getCause());
        assertFalse(error.getCause() instanceof InvocationTargetException);
        assertEquals("invalid relationship", error.getCause().getMessage());
    }
}
