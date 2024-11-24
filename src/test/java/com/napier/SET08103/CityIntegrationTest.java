package com.napier.SET08103;

import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.concepts.City;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public final class CityIntegrationTest {
    static App app;

    @BeforeAll
    static void init() {
        app = new App();

        app.connect(
                Objects.requireNonNullElse(
                        System.getenv(Constants.MYSQL_HOST_ENVAR),
                        Constants.MYSQL_HOST_ENVAR_DEFAULT),
                Objects.requireNonNullElse(
                        System.getenv(Constants.MYSQL_ROOT_PASSWORD_ENVAR),
                        Constants.MYSQL_ROOT_PASSWORD_DEFAULT)
        );
    }

    @AfterAll
    static void deInit() {
        app.close();
    }

    @Test
    void cityCreateValid() throws SQLException {
        Connection conn = app.getConnectionForIntegrationTesting();

        BiConsumer<Integer, String> createCity = (id, name) -> {
            { // fromId
                // Placeholder variable for the lambda expression
                AtomicReference<City> x = new AtomicReference<>();
                // Ensure it was created without an error
                assertAll(() -> x.set(City.fromId(id, conn)));
                // Ensure it is actually the correct city
                assertEquals(name.toLowerCase(), x.get().toString().toLowerCase());
            }
            { // fromName, same as above, but with name and id swapped
                AtomicReference<City> x = new AtomicReference<>();
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
            City ArubaCapital = City.fromId(129, conn);
            assertEquals(Zone.CAPITALS, ArubaCapital.getZoneLevel());

            assertEquals("Aruba", ArubaCapital.getOuterZone().toString());
            assertEquals(Zone.COUNTRIES, ArubaCapital.getOuterZone().getZoneLevel());
        }
        {
            // Known to not be a capital, and District is known to be non-NULL
            City Oranjestad = City.fromId(128, conn);
            assertEquals(Zone.CITIES, Oranjestad.getZoneLevel());

            assertEquals("Lori", Oranjestad.getOuterZone().toString());
            assertEquals(Zone.DISTRICTS, Oranjestad.getOuterZone().getZoneLevel());
        }
    }

    @Test
    void cityCreateInValid() {
        Connection conn = app.getConnectionForIntegrationTesting();

        // Both name and id must be invalid and create no city
        BiConsumer<Integer, String> createCity = (id, name) -> {
            assertThrows(IllegalArgumentException.class, () -> City.fromId(id, conn));
            assertThrows(IllegalArgumentException.class, () -> City.fromName(name, conn));
        };

        createCity.accept(0, "bajookieland");
        createCity.accept(-12, "");
        createCity.accept(65347860, "123");
    }

    @Test
    void cityGetPopulation() {
        Connection conn = app.getConnectionForIntegrationTesting();
        try {
            City kabul = City.fromId(1, conn);
            assertEquals(1780000, kabul.getPopulation().total);

            City london = City.fromName("LonDON", conn);
            assertEquals(7285000, london.getPopulation().total);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void cityIsCapital() {
        Connection conn = app.getConnectionForIntegrationTesting();
        try {
            City kabul = City.fromId(1, conn);
            assertTrue(kabul.isCapital());

            City london = City.fromName("LonDON", conn);
            assertTrue(london.isCapital());

            City herat = City.fromId(3, conn);
            assertFalse(herat.isCapital());

            City haag = City.fromName("haag", conn);
            assertFalse(haag.isCapital());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void cityGetCapitals() throws SQLException {
        Connection conn = app.getConnectionForIntegrationTesting();
        List<City> capitals = City.capitals(conn);
        assertTrue(Arrays.asList(capitals.stream().map(City::toString).toArray()).contains("London"));
    }
}
