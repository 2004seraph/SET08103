package com.napier.SET08103.model;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.model.concepts.City;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public final class CityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createValid() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        BiConsumer<Integer, String> createCity = (id, name) -> {
            { // fromId
                // Placeholder variable for the lambda expression
                final AtomicReference<City> x = new AtomicReference<>();
                // Ensure it was created without an error
                assertAll(() -> x.set(City.fromId(id, conn)));
                // Ensure it is actually the correct city
                assertEquals(name.toLowerCase(), x.get().toString().toLowerCase());
            }
            { // fromName, same as above, but with name and id swapped
                final AtomicReference<City> x = new AtomicReference<>();
                assertAll(() -> x.set(City.fromName(name, conn)));
                assertEquals(id, Integer.valueOf(x.get().id));
            }
        };

        createCity.accept(1, "kabul"); // First entry
        createCity.accept(15, "enschede"); // Lowercase
        createCity.accept(1394, "ARAK");  // Uppercase
        createCity.accept(1394, "arAK");  // random
        createCity.accept(2522, "León"); // Unicode
        createCity.accept(4079, "Rafah"); // Last entry

        // Test if the zoning info is set correctly

        {
            // Known to be a capital, and District is known to be NULL "-"
            final City ArubaCapital = City.fromId(129, conn);
            assertEquals(Zone.CAPITALS, ArubaCapital.getZoneLevel());

            assertEquals("Aruba", ArubaCapital.getOuterZone().toString());
            assertEquals(Zone.COUNTRIES, ArubaCapital.getOuterZone().getZoneLevel());
        }
        {
            // Known to not be a capital, and District is known to be non-NULL
            final City Oranjestad = City.fromId(128, conn);
            assertEquals(Zone.CITIES, Oranjestad.getZoneLevel());

            assertEquals("Lori", Oranjestad.getOuterZone().toString());
            assertEquals(Zone.DISTRICTS, Oranjestad.getOuterZone().getZoneLevel());
        }
    }

    @Test
    void createInValid() {
        final Connection conn = getAppDatabaseConnection();

        // Both name and id must be invalid and create no city
        BiConsumer<Integer, String> createCity = (id, name) -> {
            assertThrows(IllegalArgumentException.class, () -> City.fromId(id, conn));
            assertThrows(IllegalArgumentException.class, () -> City.fromName(name, conn));
        };

        // even with "", City.fromName() will still try to find a City
        createCity.accept(0, "bajookieland");
        createCity.accept(-12, "erghieiugfbwiefbuiwuhfiuwhrfiwhurfiwu");
        createCity.accept(-12, "");
        createCity.accept(65347860, "123");
    }

    @Test
    void getTotalPopulation() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        final City kabul = City.fromId(1, conn);
        assertEquals(1780000, kabul.getTotalPopulation(conn));

        final City london = City.fromName("LonDON", conn);
        assertEquals(7285000, london.getTotalPopulation(conn));
    }

    @Test
    void isCapital() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        final City kabul = City.fromId(1, conn);
        assertTrue(kabul.isCapital());

        final City london = City.fromName("LonDON", conn);
        assertTrue(london.isCapital());

        final City herat = City.fromId(3, conn);
        assertFalse(herat.isCapital());

        final City haag = City.fromName("haag", conn);
        assertFalse(haag.isCapital());
    }

    @Test
    void getAllCapitals() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        final List<City> capitals = City.allCapitals(conn);
        assertTrue(Arrays.asList(capitals.stream().map(City::toString).toArray()).contains("London"));
    }
}
