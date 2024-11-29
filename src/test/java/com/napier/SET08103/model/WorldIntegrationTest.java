package com.napier.SET08103.model;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.model.concepts.City;
import com.napier.SET08103.model.concepts.World;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldIntegrationTest extends AbstractIntegrationTest {

    @Test
    void zoneInfo() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        // ensure there are indeed 7 continents
        assertEquals(World.INSTANCE.getInnerZones(conn).size(), 7);
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

            assertAll(() -> World.preload(conn));
            List<City> citiesOfTheWorld = World.INSTANCE.getCities(conn);

            int total = 0;
            while (res.next()) {
                total++;
                assertTrue(citiesOfTheWorld.contains(City.fromId(res.getInt(City.PRIMARY_KEY), conn)));
            }
            assertEquals(total, citiesOfTheWorld.size());

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
