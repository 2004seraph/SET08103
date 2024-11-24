package com.napier.SET08103;

import com.napier.SET08103.model.concepts.City;
import com.napier.SET08103.model.concepts.Continent;
import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.db.Model;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public final class ContinentIntegrationTest extends AbstractIntegrationTest {

    final static List<String> northAmericaRegionNames = List.of(
            "Caribbean",
            "Central America",
            "North America");

    @Test
    void continentCreate() {
        Connection conn = app.getConnectionForIntegrationTesting();

        BiConsumer<String, String> createContinentLike = (search, actual) -> {
            // Placeholder variable for the lambda expression
            AtomicReference<Continent> x = new AtomicReference<>();
            // Ensure it was created without an error
            assertAll(() -> x.set(Continent.likeDatabaseString(search, conn)));
            // Ensure it is actually the correct city
            assertEquals(actual.toLowerCase(), x.get().toString().toLowerCase());
        };

        // likeDatabaseString()
        createContinentLike.accept("North", "North America");
        createContinentLike.accept("south", "South America");
        // NA has a higher population than SA, so it should be picked
        createContinentLike.accept("America", "North America");

        // fromDatabaseString
        assertAll(() -> assertEquals(
                Continent.fromDatabaseString("Europe").toString(),
                "Europe"));
        assertAll(() -> assertEquals(
                Continent.fromDatabaseString("South America").toString(),
                "South America"));
        assertAll(() -> assertEquals(
                Continent.fromDatabaseString("south America").toString(),
                "South America"));

        // fromEnumValue
        assertAll(() -> assertEquals(
                Continent.fromValue(Continent.FieldEnum.AFRICA).toString(),
                "Africa"));

        assertAll(() -> assertEquals(
                Continent.valueOf("AFRICA").toString(),
                "Africa"));
        assertAll(() -> assertEquals(
                Continent.valueOf("NORTH_AMERICA").toString(),
                "North America"));
    }

    @Test
    void zoneInfo() throws SQLException {
        Connection conn = app.getConnectionForIntegrationTesting();

        assertNull(Continent.fromValue(Continent.FieldEnum.ASIA).getOuterZone());

        // getInnerZones
        List<IZone> northAmericaRegions = Continent.fromDatabaseString("North America")
                .getInnerZones(conn);
        assertEquals(northAmericaRegions.size(), northAmericaRegionNames.size());
        assertEquals(
                new HashSet<>(northAmericaRegionNames),
                northAmericaRegions.stream()
                        .map(Object::toString)
                        .collect(Collectors.toCollection(HashSet::new)));

        // getCities
        Consumer<Continent> checkCities = (continent) -> {
            List<String> cityNames = new ArrayList<>();
            List<City> citiesRequest;

//            SELECT city.Name
//            FROM city
//            INNER JOIN country
//            ON city.CountryCode = country.Code
//            WHERE Continent = "South America";

            try (PreparedStatement stmt = conn.prepareStatement(
                    Model.buildStatement(
                            "SELECT", City.nameField,
                            "FROM", City.table,
                            "INNER JOIN", Country.table,
                            "ON", City.countryCodeField, "=", Country.primaryKeyField,
                            "WHERE", Country.continentField, "= ?"
                    )
            )) {
                stmt.setString(1, continent.toString());

                try (ResultSet res = stmt.executeQuery()) {
                    while (res.next())
                        cityNames.add(res.getString(City.nameField));
                }

                citiesRequest = continent.getCities(conn);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Antarctica has no cities
            if (continent.getValue() != Continent.FieldEnum.ANTARCTICA)
                assertFalse(citiesRequest.isEmpty());

            assertEquals(cityNames.size(), citiesRequest.size());
            assertTrue(Utilities.compareLists(
                    cityNames,
                    citiesRequest.stream().map(Object::toString).collect(Collectors.toList())));
        };

        checkCities.accept(Continent.fromValue(Continent.FieldEnum.NORTH_AMERICA));
        checkCities.accept(Continent.fromValue(Continent.FieldEnum.SOUTH_AMERICA));
        checkCities.accept(Continent.fromValue(Continent.FieldEnum.ANTARCTICA));
        checkCities.accept(Continent.fromValue(Continent.FieldEnum.ASIA));
        checkCities.accept(Continent.fromValue(Continent.FieldEnum.OCEANIA));
        checkCities.accept(Continent.fromValue(Continent.FieldEnum.EUROPE));
        checkCities.accept(Continent.fromValue(Continent.FieldEnum.AFRICA));
    }
}
