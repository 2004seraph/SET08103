package uk.ac.napier.SET08103.repl;

import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.Testing;
import uk.ac.napier.SET08103.model.concepts.Continent;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.Region;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.repl.commands.Command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class CommandIntegrationTest extends AbstractIntegrationTest {

    @Test
    void leaderboard() {
        final Connection conn = getAppDatabaseConnection();

        Testing.setOutputState(false);

        assertEquals(239, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "countries")).size());

        assertEquals(46, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe")).size());
        // limit
        assertEquals(10, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe", "--top", "10")).size());
        // capitals
        assertEquals(10, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "capitals", "--in", "continent:europe", "--top", "10")).size());
        assertEquals(46, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "capitals", "--in", "continent:europe")).size());

        assertEquals(7, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "continents", "--in", "world")).size());
        // default value of world
        assertEquals(7, ((List<IZone>) Repl.parseAndRun(conn,
                Command.LEADERBOARD.name(), "--of", "continents")).size());

        assertEquals(71, ((List<IZone>) Repl.parseAndRun(conn,
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
        assertEquals(51, ((List<IZone>) Repl.parseAndRun(conn,
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
        final Connection conn = getAppDatabaseConnection();

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

    @Test
    void populationInfo() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        Testing.setOutputState(false);

        // Valid zones

        assertEquals(
                Country.fromName("united kingdom", conn).getPopulationInfo(conn),
                Repl.parseAndRun(conn, Command.INFO.name(), "--in", "country:united_kingdom"));
        assertEquals(
                Continent.fromValue(Continent.FieldEnum.EUROPE).getPopulationInfo(conn),
                Repl.parseAndRun(conn, Command.INFO.name(), "--in", "continent:europe"));
        assertEquals(
                Region.fromName("Western Europe", conn).getPopulationInfo(conn),
                Repl.parseAndRun(conn, Command.INFO.name(), "--in", "region:Western_Europe"));

        // non equals case
        assertNotEquals(Continent.fromValue(Continent.FieldEnum.EUROPE).getPopulationInfo(conn),
                Repl.parseAndRun(conn, Command.INFO.name(), "--in", "region:Western_Europe"));

        // Invalid zones that do not implement IDistributedPopulation

        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--in", "city:london"))
                .getClass(), IllegalArgumentException.class);
        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--in", "district:texas"))
                .getClass(), IllegalArgumentException.class);
        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--in", "world"))
                .getClass(), IllegalArgumentException.class);

        // --of

        assertEquals(7,
                ((List<IZone>) Repl.parseAndRun(conn, Command.INFO.name(), "--of", "continents")).size());

        assertEquals(239,
                ((List<IZone>) Repl.parseAndRun(conn, Command.INFO.name(), "--of", "countries")).size());

        // invalid

        // Non IDistributedZones
        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--of", "capitals"))
                .getClass(), IllegalArgumentException.class);
        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--of", "cities"))
                .getClass(), IllegalArgumentException.class);
        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--of", "districts"))
                .getClass(), IllegalArgumentException.class);

        // wrong arg number

        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(conn, Command.INFO.name(), "--of", "continents", "--in", "world"))
                .getClass(), IllegalArgumentException.class);

        assertSame(Testing.getExceptionCause(
                        () -> Repl.parseAndRun(new String[]{Command.INFO.name()}, conn))
                .getClass(), IllegalArgumentException.class);

        Testing.setOutputState(true);
    }
}
