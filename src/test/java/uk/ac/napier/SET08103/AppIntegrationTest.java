package uk.ac.napier.SET08103;

import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.repl.commands.Command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AppIntegrationTest {

    @Test
    void checkDriver() {
        assertTrue(App.isDriverLoaded());
    }

    @Test
    void run() {
        assertAll(() ->
                App.main(new String[]{ Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe" }));
    }
}
