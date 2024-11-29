package com.napier.SET08103.model;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.Testing;
import com.napier.SET08103.model.concepts.*;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.napier.SET08103.model.concepts.zone.Zone.wrapIZone;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

public final class ZoneIntegrationTest extends AbstractIntegrationTest {

    @Test
    void equals() throws SQLException {
        Connection conn = getAppDatabaseConnection();

        assertFalse(City.fromName("Dallas", conn).equals(Country.fromCountryCode("USA", conn)));
        assertFalse(Country.fromCountryCode("USA", conn).equals(City.fromName("Dallas", conn)));

        assertTrue(City.fromId(3800, conn).equals(City.fromName("Dallas", conn)));
        assertTrue(City.fromName("Dallas", conn).equals(City.fromId(3800, conn)));
    }

    @Test
    void districtInnerTraversal() throws SQLException { // Tests method getInnerZones() on AbstractZone
        Connection conn = getAppDatabaseConnection();

        District texas = District.fromName("Texas", "USA", conn);
        assertTrue(Testing.compareLists(texas.getInnerZones(1, conn), wrapIZone(texas.getCities(conn))));
        assertTrue(Testing.compareLists(texas.getInnerZones(2, conn), wrapIZone(texas.getCities(conn))));
    }

    @Test
    void regionInnerTraversal() throws SQLException { // Tests method getInnerZones() on AbstractZone
        Connection conn = getAppDatabaseConnection();

        Region caribbean = Region.fromName("Caribbean", conn);
        assertTrue(Testing.compareLists(caribbean.getInnerZones(1, conn), caribbean.getInnerZones(conn)));
    }

    @Test
    void continentInnerTraversal() throws SQLException { // Tests method getInnerZones() on AbstractZone
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
                            wrapIZone(Continent.fromValue(continent).getCities(conn)))
                    );

                    // Ensure it bottoms out at the city level and just returns the same thing each time
                    assertTrue(Testing.compareLists(
                            continentToCities,
                            Continent.fromValue(continent).getInnerZones(5, conn))
                    );
                    assertTrue(Testing.compareLists(
                            continentToCities,
                            Continent.fromValue(continent).getInnerZones(6, conn))
                    );
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        for (Continent.FieldEnum c : Continent.FieldEnum.asList)
            assertAll(() -> testContinents.accept(c));
    }
}
