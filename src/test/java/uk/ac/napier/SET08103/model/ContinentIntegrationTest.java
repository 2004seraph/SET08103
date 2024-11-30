package uk.ac.napier.SET08103.model;

import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.Continent;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.types.PopulationInfo;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.Testing;
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

    @SuppressWarnings("ExtractMethodRecommender")
    @Test
    void continentCreate() {
        Connection conn = getAppDatabaseConnection();

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
        Connection conn = getAppDatabaseConnection();

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
                            "SELECT", City.NAME,
                            "FROM", City.TABLE,
                            "INNER JOIN", Country.TABLE,
                            "ON", City.COUNTRY_CODE, "=", Country.PRIMARY_KEY,
                            "WHERE", Country.CONTINENT, "= ?"
                    )
            )) {
                stmt.setString(1, continent.toString());

                try (ResultSet res = stmt.executeQuery()) {
                    while (res.next())
                        cityNames.add(res.getString(City.NAME));
                }

                citiesRequest = continent.getCities(conn);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Antarctica has no cities
            if (continent.getValue() != Continent.FieldEnum.ANTARCTICA)
                assertFalse(citiesRequest.isEmpty());

            assertEquals(cityNames.size(), citiesRequest.size());
            assertTrue(Testing.compareLists(
                    cityNames,
                    citiesRequest.stream().map(Object::toString).collect(Collectors.toList())));
        };

        // might as well just check every single continent
        for (Continent.FieldEnum c : Continent.FieldEnum.asArray)
            checkCities.accept(Continent.fromValue(c));
    }

    @Test
    void getPopulation() throws SQLException {
        Connection conn = getAppDatabaseConnection();

        // population in cities
//        SELECT country.Continent, SUM(city.Population) AS CityPop
//        FROM city
//        INNER JOIN country
//        ON country.Code = city.CountryCode
//        GROUP BY country.Continent

        // total population
//        SELECT country.Continent , SUM(country.Population) AS Total
//        FROM country
//        GROUP BY country.Continent
//        ORDER BY country.Continent

        PopulationInfo naInfo = Continent.fromValue(Continent.FieldEnum.NORTH_AMERICA).getPopulationInfo(conn);
        assertEquals(482993000,
                naInfo.total);
        assertEquals(168250381,
                naInfo.inCities);
        assertEquals(482993000 - 168250381,
                naInfo.outsideCities);

        // The population of Asia is bigger than a 32-bit integer
        PopulationInfo asiaInfo = Continent.fromValue(Continent.FieldEnum.ASIA).getPopulationInfo(conn);
        assertEquals(3705025700L,
                asiaInfo.total);
        assertEquals(697604103,
                asiaInfo.inCities);
        assertEquals(3705025700L - 697604103,
                asiaInfo.outsideCities);

        // No population
        assertEquals(0,
                Continent.fromValue(Continent.FieldEnum.ANTARCTICA).getPopulationInfo(conn).total);
    }
}
