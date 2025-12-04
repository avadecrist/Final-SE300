package com.se300.store.model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CommandProcessorIOExceptionTest {

    @Test
    public void processCommandFile_nonexistentFile_printsStackTraceAndReturns() {
        CommandProcessor cp = new CommandProcessor();

        // Generate a filename that should not exist
        String fname = Path.of("nonexistent_command_file_" + System.currentTimeMillis() + ".script").toAbsolutePath().toString();

        // Capture stderr to verify stacktrace was printed
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos));

        try {
            assertDoesNotThrow(() -> cp.processCommandFile(fname));

            String errOutput = baos.toString();
            // The implementation prints stacktrace on IOException — the class name should appear
            assertTrue(errOutput.contains("NoSuchFileException") || errOutput.toLowerCase().contains("ioexception"),
                    "Expected stacktrace mentioning NoSuchFileException or IOException, got: " + errOutput);
        } finally {
            // restore stderr
            System.setErr(oldErr);
        }
    }
}
