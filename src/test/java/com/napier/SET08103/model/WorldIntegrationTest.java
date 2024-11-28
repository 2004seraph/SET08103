package com.napier.SET08103.model;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.model.concepts.World;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldIntegrationTest extends AbstractIntegrationTest {

    @Test
    void zoneInfo() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        // ensure there are indeed 7 continents
        assertEquals(World.INSTANCE.getInnerZones(conn).size(), 7);
    }

    @Test
    void getPopulation() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        assertEquals(World.INSTANCE.getTotalPopulation(conn), 6078749450L);
    }
}
