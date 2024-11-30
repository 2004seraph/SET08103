package uk.ac.napier.SET08103.model;

import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.Testing;
import uk.ac.napier.SET08103.model.concepts.*;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.model.concepts.zone.Zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static uk.ac.napier.SET08103.model.concepts.zone.Zone.wrapIZone;

public final class ZoneIntegrationTest extends AbstractIntegrationTest {

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    public void equals() throws SQLException {
        Connection conn = getAppDatabaseConnection();

        assertNotEquals(City.fromName("Dallas", conn), Country.fromCountryCode("USA", conn));
        assertNotEquals(Country.fromCountryCode("USA", conn), City.fromName("Dallas", conn));

        assertEquals(City.fromId(3800, conn), City.fromName("Dallas", conn));
        assertEquals(City.fromName("Dallas", conn), City.fromId(3800, conn));
    }

    @Test
    public void districtInnerTraversal() throws SQLException { // Tests method getInnerZones() on AbstractZone
        Connection conn = getAppDatabaseConnection();

        District texas = District.fromName("Texas", "USA", conn);
        assertTrue(Testing.compareLists(texas.getInnerZones(1, conn), wrapIZone(texas.getCities(conn))));
        assertTrue(Testing.compareLists(texas.getInnerZones(2, conn), wrapIZone(texas.getCities(conn))));
    }

    @Test
    public void regionInnerTraversal() throws SQLException { // Tests method getInnerZones() on AbstractZone
        Connection conn = getAppDatabaseConnection();

        Region caribbean = Region.fromName("Caribbean", conn);
        assertTrue(Testing.compareLists(caribbean.getInnerZones(1, conn), caribbean.getInnerZones(conn)));
    }

    @Test
    public void continentInnerTraversal() throws SQLException { // Tests method getInnerZones() on AbstractZone
        Connection conn = getAppDatabaseConnection();

        assertTrue(Testing.compareLists(
                World.INSTANCE.getInnerZones(1, conn),
                Continent.getAllAsIZones()));

        Consumer<Continent.FieldEnum> testContinents = (continent) -> {
            try (PreparedStatement stmt = conn.prepareStatement(Model.buildStatement(
                    "SELECT", Country.PRIMARY_KEY,
                    "FROM", Country.TABLE,
                    "WHERE", Country.CONTINENT, "= ?"
            ))) {
                stmt.setString(1, continent.getDatabaseName());

                try(stmt; ResultSet res = stmt.executeQuery()) {
                    List<IZone> countries = new ArrayList<>();

                    { // Checking the specific countries retrieved
                        while (res.next())
                            countries.add(Country.fromCountryCode(res.getString(Country.PRIMARY_KEY), conn));
                        assertTrue(Testing.compareLists(
                                Continent.fromValue(continent).getInnerZones(2, conn),
                                countries));
                    }

                    // assuring the correct types are retrieved for each level

                    assertTrue(Continent.fromValue(continent).getInnerZones(1, conn).stream().allMatch(
                            z -> z.getZoneLevel() == Zone.REGIONS
                    ));

                    // Assure all are districts, or cities/capitals with no parent district
                    assertTrue(Continent.fromValue(continent).getInnerZones(3, conn).stream().allMatch(
                            z -> z.getZoneLevel() == Zone.DISTRICTS ||
                                    (z.getZoneLevel() == Zone.CITIES || z.getZoneLevel() == Zone.CAPITALS) &&
                                            (((City) z).getDistrict() == null)
                    ));

                    // 4 levels down from a Continent should be only cities
                    var continentToCities = Continent.fromValue(continent).getInnerZones(4, conn);
                    assertTrue(Testing.compareLists(
                            continentToCities,
                            wrapIZone(Continent.fromValue(continent).getCities(conn))));

                    // Ensure it bottoms out at the city level and just returns the same thing each time
                    assertTrue(Testing.compareLists(
                            continentToCities,
                            Continent.fromValue(continent).getInnerZones(5, conn)));
                    assertTrue(Testing.compareLists(
                            continentToCities,
                            Continent.fromValue(continent).getInnerZones(6, conn)));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        for (Continent.FieldEnum c : Continent.FieldEnum.asArray)
            assertAll(() -> testContinents.accept(c));
    }
}
