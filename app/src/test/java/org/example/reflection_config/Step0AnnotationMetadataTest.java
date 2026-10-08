package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step0AnnotationMetadataTest {
    @Test
    void exerciseAnnotationsHaveRuntimeMetadataAndCorrectTargets() {
        assertEquals(
                RetentionPolicy.RUNTIME,
                ConfigProperty.class.getAnnotation(Retention.class).value()
        );
        assertEquals(
                RetentionPolicy.RUNTIME,
                AfterBinding.class.getAnnotation(Retention.class).value()
        );
        assertTrue(hasTarget(ConfigProperty.class, ElementType.FIELD));
        assertTrue(hasTarget(AfterBinding.class, ElementType.METHOD));
    }

    private static boolean hasTarget(Class<?> annotationType, ElementType expectedTarget) {
        return Arrays.asList(annotationType.getAnnotation(Target.class).value())
                .contains(expectedTarget);
    }
}
