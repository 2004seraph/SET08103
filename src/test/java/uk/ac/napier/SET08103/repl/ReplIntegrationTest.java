package uk.ac.napier.SET08103.repl;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.Testing;
import uk.ac.napier.SET08103.model.concepts.*;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.repl.commands.Command;
import uk.ac.napier.SET08103.repl.commands.ICommand;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.BiConsumer;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;


public final class ReplIntegrationTest extends AbstractIntegrationTest {
    @Test
    void disconnected() throws SQLException {
        final Connection conn = getAppDatabaseConnection();
        conn.close();
        Testing.setOutputState(false);

        assertTrue(SQLException.class.isAssignableFrom(
                Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "city:london")).getClass()));

        // Reconnect after this test
        connectToDatabase();
        Testing.setOutputState(true);
    }

    @Test
    void invalidSubCommand() {
        final Connection conn = getAppDatabaseConnection();

        Testing.setOutputState(false);

        // no args
        assertThrows(Error.class,() -> Repl.parseAndRun(conn, new String[] {}));

        // Unknown subcommand
        assertEquals(IllegalArgumentException.class, Testing.getExceptionCause(() -> Repl.parseAndRun(conn, "among")).getClass());
        assertEquals(IllegalArgumentException.class, Testing.getExceptionCause(() -> Repl.parseAndRun(conn, "")).getClass());

        // Valid command, but invalid sub args
        assertTrue(ParseException.class.isAssignableFrom( // ParseException is the superclass of MissingOptionException
                Testing.getExceptionCause(() ->
                        Repl.parseAndRun(conn, Command.LEADERBOARD.name())).getClass()));
        assertTrue(ParseException.class.isAssignableFrom(
                Testing.getExceptionCause(() ->
                        Repl.parseAndRun(conn, Command.LEADERBOARD.name(), "--apple")).getClass()));

        // valid command, valid sub arg syntax, invalid semantics
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, Command.LEADERBOARD.name(), "--of", "pacific"));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, Command.LEADERBOARD.name(), "--of"));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, Command.LEADERBOARD.name(), "--of", ""));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, Command.LEADERBOARD.name(), "--of", "america"));

        Testing.setOutputState(true);
    }

    @Test
    void parseZoneReference() {
        final Connection conn = getAppDatabaseConnection();

        final BiConsumer<String, IZone> tryParseZoneRef = (ref, actual) -> {
            IZone retrievedZone;
            try {
                final CommandLine args = parseCommandLine(new TestCommand(), new String[]{ "--in", ref });
                final Properties p = args.getOptionProperties("in");

                retrievedZone = Repl.parseZoneReference(p, conn);

            } catch (ParseException | SQLException e) {
                throw new RuntimeException(e);
            }

            assertEquals(retrievedZone, actual);
        };

        // Syntax error
        assertThrows(RuntimeException.class, () ->
                tryParseZoneRef.accept("space:sun", World.INSTANCE));
        assertThrows(RuntimeException.class, () ->
                tryParseZoneRef.accept("space", World.INSTANCE));
        assertThrows(RuntimeException.class, () ->
                tryParseZoneRef.accept("texas:district", World.INSTANCE));

        // Semantic error
        assertThrows(IllegalArgumentException.class, () ->
                tryParseZoneRef.accept("continent", World.INSTANCE));
        assertThrows(IllegalArgumentException.class, () ->
                tryParseZoneRef.accept("continent:Atlantis", World.INSTANCE));

        // Valid

        assertAll(() ->
                tryParseZoneRef.accept("world:this_part_is_ignored", World.INSTANCE));
        assertAll(() ->
                tryParseZoneRef.accept("world:Europe", World.INSTANCE));
        assertAll(() ->
                tryParseZoneRef.accept("world:", World.INSTANCE));

        // Enum types

        // Single word
        assertAll(() ->
                tryParseZoneRef.accept("continent:Europe", Continent.FieldEnum.EUROPE.getInstance()));
        // Spaces
        assertAll(() ->
                tryParseZoneRef.accept("continent:NORTH_AMERICA", Continent.FieldEnum.NORTH_AMERICA.getInstance()));
        assertAll(() ->
                tryParseZoneRef.accept("continent:north_america", Continent.FieldEnum.NORTH_AMERICA.getInstance()));
        // Inexact match, prefer higher population
        assertAll(() ->
                tryParseZoneRef.accept("continent:america", Continent.FieldEnum.NORTH_AMERICA.getInstance()));
        // Regions
        assertAll(() ->
                tryParseZoneRef.accept("region:caribbean", Region.fromName("caribbean", conn)));
        // Districts
        assertAll(() ->
                tryParseZoneRef.accept("district:teXaS", District.fromName("Texas", "USA", conn)));

        // Entity types

        // Country code
        assertAll(() ->
                tryParseZoneRef.accept("Country:USA", Country.fromCountryCode("USA", conn)));
        assertAll(() ->
                tryParseZoneRef.accept("Country:ZWE", Country.fromCountryCode("ZWE", conn)));
        // Name
        assertAll(() ->
                tryParseZoneRef.accept("Country:united_states", Country.fromCountryCode("USA", conn)));
        assertAll(() ->
                tryParseZoneRef.accept("Country:united", Country.fromCountryCode("USA", conn)));
        assertAll(() ->
                tryParseZoneRef.accept("Country:Zimbabwe", Country.fromCountryCode("ZWE", conn)));

        // city IDs
        assertAll(() ->
                tryParseZoneRef.accept("CITY:521", City.fromId(521, conn)));
        assertAll(() ->
                tryParseZoneRef.accept("CITY:COLCHESTER", City.fromId(521, conn)));
    }

    private static class TestCommand implements ICommand {
        @Override
        public Options getOptions() {
            return new Options()
                    // --in continent:Europe
                    .addOption(
                            Option.builder("i")
                                    .longOpt("in")
                                    .hasArgs()
                                    .numberOfArgs(2)
                                    .argName("<world/continent/region>:[]")
                                    .valueSeparator(':')
                                    .build()
                    );
        }

        @Override
        public Object execute(final CommandLine args, final Connection conn) throws InternalError {
            return null;
        }
    }

    private CommandLine parseCommandLine(final ICommand command, final String[] args) throws ParseException {
        return new DefaultParser().parse(
                command.getOptions(),
                args,
                false // true = throw, false so it ignores the first arg
        );
    }
}
