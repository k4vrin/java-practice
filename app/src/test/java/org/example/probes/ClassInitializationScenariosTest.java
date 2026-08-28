package org.example.probes;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassInitializationScenariosTest {

    @Test
    void readingCompileTimeConstantDoesNotInitializeTheClasses() throws Exception {
        assertScenario("compile-time-constant", "value=21", "events=[]");
    }

    @Test
    void readingInheritedNonconstantFieldInitializesOnlyDeclaringSuperclass() throws Exception {
        assertScenario(
                "inherited-nonconstant",
                "value=parent value",
                "events=[parent initialized]"
        );
    }

    @Test
    void readingSubclassNonconstantFieldInitializesSuperclassThenSubclass() throws Exception {
        assertScenario(
                "subclass-nonconstant",
                "value=child value",
                "events=[parent initialized, child initialized]"
        );
    }

    @Test
    void repeatingAccessAfterSuccessfulInitializationDoesNotRerunInitializers() throws Exception {
        assertScenario(
                "repeated-subclass-access",
                "value=child value,child value",
                "events=[parent initialized, child initialized]"
        );
    }

    private static void assertScenario(String scenario, String... expectedLines) throws Exception {
        Process process = new ProcessBuilder(
                System.getProperty("java.home") + "/bin/java",
                "-cp",
                System.getProperty("java.class.path"),
                ClassInitializationScenarios.class.getName(),
                scenario
        ).redirectErrorStream(true).start();

        String output = readOutput(process);

        assertEquals(0, process.waitFor(), output);
        assertEquals(List.of(expectedLines), output.lines().toList());
    }

    private static String readOutput(Process process) throws IOException {
        try (var input = process.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).strip();
        }
    }
}
