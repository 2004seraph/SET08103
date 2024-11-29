package com.napier.SET08103.repl;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.Testing;
import com.napier.SET08103.model.concepts.*;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public final class ReplIntegrationTest extends AbstractIntegrationTest {


    @Test
    void invalidSubCommand() {
        Connection conn = getAppDatabaseConnection();

        // no args
        assertThrows(Error.class,() -> Repl.parseAndRun(conn, new String[] {}));

        // Unknown subcommand
        assertEquals(IllegalArgumentException.class,
                Testing.getExceptionCause(() -> Repl.parseAndRun(conn, "among")).getClass());
        assertEquals(IllegalArgumentException.class,
                Testing.getExceptionCause(() -> Repl.parseAndRun(conn, "")).getClass());

        // Valid command, but invalid sub args
        assertTrue(ParseException.class.isAssignableFrom( // ParseException is the superclass of MissingOptionException
                Testing.getExceptionCause(() -> Repl.parseAndRun(conn, "leaderboard")).getClass()));
        assertTrue(ParseException.class.isAssignableFrom(
                Testing.getExceptionCause(() -> Repl.parseAndRun(conn, "leaderboard", "--apple")).getClass()));

        // valid command, valid sub arg syntax, invalid semantics
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, "leaderboard", "--of", "pacific"));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, "leaderboard", "--of"));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, "leaderboard", "--of", ""));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn, "leaderboard", "--of", "america"));
    }

    @Test
    void parseZoneReference() {
        Connection conn = getAppDatabaseConnection();

        BiConsumer<String, IZone> tryParseZoneRef = (ref, actual) -> {
            CommandLine args;
            IZone retrievedZone;
            try {
                args = parseCommandLine(new TestCommand(), new String[]{ "--in", ref });
                Properties p = args.getOptionProperties("in");
                retrievedZone = Repl.parseZoneReference(p, conn);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
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

    private class TestCommand implements ICommand {
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
        public Object execute(CommandLine args, Connection conn) throws SQLException, InternalError, ParseException {
            return null;
        }
    }

    private CommandLine parseCommandLine(ICommand command, String[] args) throws ParseException {
        return new DefaultParser().parse(
                command.getOptions(),
                args,
                false // true = throw, false so it ignores the first arg
        );
    }
}
