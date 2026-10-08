package org.example.reflection_config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.example.reflection_config.ReflectionConfigFixtures.Mode;
import static org.example.reflection_config.ReflectionConfigFixtures.ProductionServiceConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Step6FullBinderTest {
    private final ReflectiveConfigBinder binder = new ReflectiveConfigBinder();

    @Test
    void combinesConstructionHierarchyConversionDefaultsAndLifecycleCallback() {
        ProductionServiceConfig config = binder.bind(ProductionServiceConfig.class, Map.of(
                "service.name", "payments",
                "service.port", "8443",
                "service.secure", "true",
                "service.timeoutMillis", "2500",
                "service.mode", "PRODUCTION"
        ));

        assertEquals("payments", config.name());
        assertEquals(8443, config.port());
        assertTrue(config.secure());
        assertEquals(2500L, config.timeoutMillis());
        assertEquals(Mode.PRODUCTION, config.mode());
        assertEquals("global", config.region());
        assertTrue(config.initialized());
    }
}
