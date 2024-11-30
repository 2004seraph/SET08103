package uk.ac.napier.SET08103.model;

import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.model.concepts.Continent;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.Region;
import uk.ac.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class PopulationIntegrationTest extends AbstractIntegrationTest {
    @Test
    void equals() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        // different zone type
        assertNotEquals(
                Country.fromCountryCode("USA", conn).getPopulationInfo(conn),
                Continent.fromValue(Continent.FieldEnum.EUROPE).getPopulationInfo(conn));

        // Same zone type, different zone instance
        assertNotEquals(
                Country.fromCountryCode("USA", conn).getPopulationInfo(conn),
                Country.fromCountryCode("AUS", conn).getPopulationInfo(conn));

        // true case, same underlying zone instance
        assertEquals(
                Country.fromCountryCode("USA", conn).getPopulationInfo(conn),
                Country.fromCountryCode("USA", conn).getPopulationInfo(conn));
    }

    @Test
    void compareTo() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        final BiConsumer<IDistributedPopulation, IDistributedPopulation> testComparison = (zone1, zone2) -> {
            try {
                assertEquals(
                        ((IZone)zone1).compareTo((IZone) zone2),
                        zone1.getPopulationInfo(conn).compareTo(zone2.getPopulationInfo(conn)));
                assertEquals(
                        ((IZone)zone2).compareTo((IZone) zone1),
                        zone2.getPopulationInfo(conn).compareTo(zone1.getPopulationInfo(conn)));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        // same zone type
        testComparison.accept(
                Country.fromCountryCode("USA", conn),
                Country.fromCountryCode("AUS", conn)
        );

        // different zone types
        testComparison.accept(
                Country.fromCountryCode("USA", conn),
                Continent.fromValue(Continent.FieldEnum.EUROPE)
        );
        testComparison.accept(
                Region.fromName("Antarctica", conn),
                Continent.fromValue(Continent.FieldEnum.ASIA)
        );
    }
}
