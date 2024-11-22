package com.napier.SET08103;

import com.napier.SET08103.model.concepts.City;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class AppIntegrationTest {
    static App app;

    @BeforeAll
    static void init() {
        app = new App();

        app.connect(
                Objects.requireNonNullElse(
                        System.getenv("MYSQL_HOST"),
                        "localhost"),
                Objects.requireNonNullElse(
                        System.getenv("MYSQL_ROOT_PASSWORD"),
                        "root")
        );
    }

    @AfterAll
    static void deInit() {
        app.close();
    }

    @Test
    void getPopulation() {
        Connection conn = app.getConnectionForTesting();
        try {
            City kabul = City.fromId(1, conn);
            Assertions.assertEquals(1780000, kabul.getPopulation().total);

            City london = City.fromName("LonDON", conn);
            Assertions.assertEquals(7285000, london.getPopulation().total);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void isCapital() {
        Connection conn = app.getConnectionForTesting();
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
    void getCapitals() {
        Connection conn = app.getConnectionForTesting();
        try {
            List<City> capitals = City.capitals(conn);
            assertTrue(Arrays.asList(capitals.stream().map(City::toString).toArray()).contains("London"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
