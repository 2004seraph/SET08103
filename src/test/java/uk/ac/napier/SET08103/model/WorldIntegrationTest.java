package uk.ac.napier.SET08103.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.Testing;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.ac.napier.SET08103.model.concepts.zone.Zone.wrapIZone;

public final class WorldIntegrationTest extends AbstractIntegrationTest {

    @Test
    void zoneInfo() {
        final Connection conn = getAppDatabaseConnection();

        // ensure there are indeed 7 continents
        assertEquals(World.INSTANCE.getInnerZones(conn).size(), 7);

        assertNull(World.INSTANCE.getOuterZone());
    }

    @Test
    void cities() {
        final Connection conn = getAppDatabaseConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT", City.PRIMARY_KEY,
                        "FROM", City.TABLE
                )
        ); ResultSet res = stmt.executeQuery()) {

            final List<City> citiesOfTheWorld = World.INSTANCE.getCities(conn);

            int total = 0;
            while (res.next()) {
                total++;
                Assertions.assertTrue(citiesOfTheWorld.contains(City.fromId(res.getInt(City.PRIMARY_KEY), conn)));
            }
            assertEquals(total, citiesOfTheWorld.size());

            // test caching
            final List<City> citiesOfTheWorldFromCache = World.INSTANCE.getCities(conn);
            Assertions.assertTrue(
                    Testing.compareLists(wrapIZone(citiesOfTheWorldFromCache), wrapIZone(citiesOfTheWorld)));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPopulation() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        assertEquals(World.INSTANCE.getTotalPopulation(conn), 6078749450L);
    }
}
