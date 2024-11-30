package com.napier.SET08103.repl;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.Testing;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.Command;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@SuppressWarnings("unchecked")
public class CommandIntegrationTest extends AbstractIntegrationTest {

    @Test
    void leaderboard() {
        Connection conn = getAppDatabaseConnection();

        Testing.setOutputState(false);

        assertEquals(239,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "countries")).size());

        assertEquals(46,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe")).size());
        // limit
        assertEquals(10,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe", "--top", "10")).size());
        // capitals
        assertEquals(10,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "capitals", "--in", "continent:europe", "--top", "10")).size());
        assertEquals(46,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "capitals", "--in", "continent:europe")).size());

        assertEquals(7,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "continents", "--in", "world")).size());
        // default value of world
        assertEquals(7,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "continents")).size());

        assertEquals(71,
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--in", "district:england", "--of", "cities")).size());

        // non existent zone
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "cities", "--in", "country:samLand"));

        // absent required --of parameter
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--in", "country:united_kingdom"));

        // negative limit
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe", "--top", "-10"));

        // duplicate parameters (--in is the only one that can have multiple)
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe", "--in", "region:north_america"));
        assertEquals(51, // a valid duplicate parameter will override the previous one, expected UNIX behaviour
                ((List<IZone>)Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe", "--in", "continent:asia")).size());

        // Impossible queries
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--of", "continents", "--in", "country:abw"));
        assertThrows(RuntimeException.class,
                () -> Repl.parseAndRun(conn,
                        Command.LEADERBOARD.name(), "--in", "city:london", "--of", "districts"));

        Testing.setOutputState(true);
    }

    @Test
    void populationOf() {
        Connection conn = getAppDatabaseConnection();
        Testing.setOutputState(false);

        assertEquals(7285000,
                ((Long) Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "city:london")).longValue());

        assertEquals(9208281,
                ((Long) Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "district:texas")).longValue());

        assertEquals(278357000,
                ((Long) Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "country:usa")).longValue());

        assertEquals(144674200,
                ((Long) Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "region:europe")).longValue());

        assertEquals(3705025700L,
                ((Long) Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "continent:asia")).longValue());

        assertEquals(6078749450L,
                ((Long) Repl.parseAndRun(conn, Command.TOTAL.name(), "--in", "world")).longValue());

        Testing.setOutputState(true);
    }
}