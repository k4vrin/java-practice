package org.example.reflection_config;

import java.time.Duration;

final class ReflectionConfigFixtures {
    private ReflectionConfigFixtures() {
    }

    enum Mode {
        DEVELOPMENT,
        PRODUCTION
    }

    static final class PrivateConstructorConfig {
        private final String state;

        private PrivateConstructorConfig() {
            state = "constructed";
        }

        String state() {
            return state;
        }
    }

    static final class NoDefaultConstructorConfig {
        private NoDefaultConstructorConfig(String value) {
        }
    }

    static final class ThrowingConstructorConfig {
        private ThrowingConstructorConfig() {
            throw new IllegalArgumentException("construction rejected");
        }
    }

    static final class BasicConfig {
        @ConfigProperty("service.name")
        private String name;

        String name() {
            return name;
        }
    }

    static final class OptionalConfig {
        @ConfigProperty(value = "host", required = false)
        private String host = "localhost";

        String host() {
            return host;
        }
    }

    static final class ScalarConfig {
        @ConfigProperty("primitive.int")
        private int primitiveInt;

        @ConfigProperty("boxed.int")
        private Integer boxedInt;

        @ConfigProperty("primitive.long")
        private long primitiveLong;

        @ConfigProperty("boxed.long")
        private Long boxedLong;

        @ConfigProperty("primitive.boolean")
        private boolean primitiveBoolean;

        @ConfigProperty("boxed.boolean")
        private Boolean boxedBoolean;

        @ConfigProperty("mode")
        private Mode mode;

        int primitiveInt() {
            return primitiveInt;
        }

        Integer boxedInt() {
            return boxedInt;
        }

        long primitiveLong() {
            return primitiveLong;
        }

        Long boxedLong() {
            return boxedLong;
        }

        boolean primitiveBoolean() {
            return primitiveBoolean;
        }

        Boolean boxedBoolean() {
            return boxedBoolean;
        }

        Mode mode() {
            return mode;
        }
    }

    static final class BooleanConfig {
        @ConfigProperty("feature.enabled")
        private boolean enabled;
    }

    static final class IntegerConfig {
        @ConfigProperty("workers")
        private int workers;
    }

    static final class UnsupportedTypeConfig {
        @ConfigProperty("timeout")
        private Duration timeout;
    }

    static class BaseConfig {
        @ConfigProperty("service.name")
        private String name;

        String name() {
            return name;
        }
    }

    static final class ChildConfig extends BaseConfig {
        @ConfigProperty("service.retries")
        private int retries;

        int retries() {
            return retries;
        }
    }

    static final class StaticFieldConfig {
        @ConfigProperty("global")
        static String global = "original";
    }

    static final class FinalFieldConfig {
        @ConfigProperty("name")
        private final String name = "original";
    }

    static class DuplicateBaseConfig {
        @ConfigProperty("shared")
        private String parentValue;
    }

    static final class DuplicateChildConfig extends DuplicateBaseConfig {
        @ConfigProperty("shared")
        private String childValue;
    }

    static final class ValidatedConfig {
        @ConfigProperty("min")
        private int min;

        @ConfigProperty("max")
        private int max;

        private boolean validated;
        private int range;

        @AfterBinding
        private void validateAndCalculate() {
            validated = true;
            range = max - min;
        }

        boolean validated() {
            return validated;
        }

        int range() {
            return range;
        }
    }

    static class CallbackBaseConfig {
        private boolean callbackInvoked;

        @AfterBinding
        private void afterBinding() {
            callbackInvoked = true;
        }

        boolean callbackInvoked() {
            return callbackInvoked;
        }
    }

    static final class InheritedCallbackConfig extends CallbackBaseConfig {
        @ConfigProperty("label")
        private String label;
    }

    static class ParentWithCallback {
        @AfterBinding
        private void parentCallback() {
        }
    }

    static final class MultipleCallbacksConfig extends ParentWithCallback {
        @AfterBinding
        private void childCallback() {
        }
    }

    static final class InvalidCallbackConfig {
        @AfterBinding
        private void invalid(String argument) {
        }
    }

    static final class ThrowingCallbackConfig {
        @AfterBinding
        private void fail() {
            throw new IllegalStateException("invalid relationship");
        }
    }

    static class ProductionBaseConfig {
        @ConfigProperty("service.name")
        private String name;

        String name() {
            return name;
        }
    }

    static final class ProductionServiceConfig extends ProductionBaseConfig {
        @ConfigProperty("service.port")
        private int port;

        @ConfigProperty("service.secure")
        private boolean secure;

        @ConfigProperty("service.timeoutMillis")
        private Long timeoutMillis;

        @ConfigProperty("service.mode")
        private Mode mode;

        @ConfigProperty(value = "service.region", required = false)
        private String region = "global";

        private boolean initialized;

        @AfterBinding
        private void initialize() {
            initialized = true;
        }

        int port() {
            return port;
        }

        boolean secure() {
            return secure;
        }

        Long timeoutMillis() {
            return timeoutMillis;
        }

        Mode mode() {
            return mode;
        }

        String region() {
            return region;
        }

        boolean initialized() {
            return initialized;
        }
    }
}
