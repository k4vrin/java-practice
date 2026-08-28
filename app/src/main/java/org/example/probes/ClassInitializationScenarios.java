package org.example.probes;

import java.util.ArrayList;
import java.util.List;

/**
 * Starts exactly one class-initialization scenario per JVM invocation.
 * Keeping scenarios in separate processes prevents a successful initialization
 * in one scenario from affecting the next scenario's observation.
 */
public final class ClassInitializationScenarios {

    private ClassInitializationScenarios() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one scenario name");
        }

        String value = switch (args[0]) {
            case "compile-time-constant" -> String.valueOf(Child.COMPILE_TIME_CONSTANT);
            case "inherited-nonconstant" -> String.valueOf(Child.PARENT_NON_CONSTANT);
            case "subclass-nonconstant" -> String.valueOf(Child.CHILD_NON_CONSTANT);
            case "repeated-subclass-access" ->
                    Child.CHILD_NON_CONSTANT + "," + Child.CHILD_NON_CONSTANT;
            default -> throw new IllegalArgumentException("Unknown scenario: " + args[0]);
        };

        System.out.println("value=" + value);
        System.out.println("events=" + InitializationEvents.events());
    }

    private static final class InitializationEvents {
        private static final List<String> EVENTS = new ArrayList<>();

        private static void record(String event) {
            EVENTS.add(event);
        }

        private static List<String> events() {
            return List.copyOf(EVENTS);
        }
    }

    static class Parent {
        static final String PARENT_NON_CONSTANT = createParentValue();

        static {
            InitializationEvents.record("parent initialized");
        }

        private static String createParentValue() {
            return "parent value";
        }
    }

    static final class Child extends Parent {
        static final int COMPILE_TIME_CONSTANT = 21;
        static final String CHILD_NON_CONSTANT = createChildValue();

        static {
            InitializationEvents.record("child initialized");
        }

        private static String createChildValue() {
            return "child value";
        }
    }
}
