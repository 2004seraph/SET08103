package uk.ac.napier.SET08103.repl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;

public final class ReplUnitTest {
    @Test
    void testBasicOutput() {
        // Ensure this doesn't fail, as it would be impossible to handle
        assertAll(Repl::printWelcome);
    }
}
