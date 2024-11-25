package com.napier.SET08103;

import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.concepts.City;
import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.model.concepts.Region;
import com.napier.SET08103.model.concepts.zone.IZone;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

final class RegionIntegrationTest extends AbstractIntegrationTest {

    final static List<String> northAmericaCountryNames = List.of(
            "Bermuda",
            "Canada",
            "Greenland",
            "Saint Pierre and Miquelon",
            "United States");

    @Test
    void regionCreate() {
        Connection conn = app.getConnectionForIntegrationTesting();

        // no spaces
        assertAll(() -> Region.fromName("Polynesia", conn));
        // spaces
        assertAll(() -> Region.fromName("Middle East", conn));

        // Misspelling
        assertThrows(IllegalArgumentException.class, () ->
                Region.fromName("SouthernEurope", conn));
        assertThrows(IllegalArgumentException.class, () ->
                Region.fromName("Middle", conn));

        // non-existent
        assertThrows(IllegalArgumentException.class, () ->
                Region.fromName("Volgograd", conn));

        // Invalid
        assertThrows(IllegalArgumentException.class, () ->
                Region.fromName("", conn));
    }


    @Test
    void zoneInfo() throws SQLException {
        final Connection conn = app.getConnectionForIntegrationTesting();

        // getInnerZones()
        List<IZone> northAmericaCountries = Region.fromName("North America", conn)
                .getInnerZones(conn);

        assertEquals(northAmericaCountries.size(), northAmericaCountryNames.size());
        assertEquals(
                new HashSet<>(northAmericaCountryNames),
                northAmericaCountries.stream()
                        .map(Object::toString)
                        .collect(Collectors.toCollection(HashSet::new)));

        // getCities()
        Consumer<Region> checkCities = (region) -> {
            List<String> cityNames = new ArrayList<>();
            List<City> citiesRequest;

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT " + City.NAME +
                            " FROM " + City.TABLE +
                            " INNER JOIN " + Country.TABLE +
                            " ON " + Country.PRIMARY_KEY +
                            " = " + City.COUNTRY_CODE +
                            " WHERE " + Country.REGION + " = ?"
            )) {
                stmt.setString(1, region.toString());

                try (ResultSet res = stmt.executeQuery()) {
                    while (res.next())
                        cityNames.add(res.getString(City.NAME));
                }

                //citiesRequest.stream().map(Object::toString).filter(e -> !cityNames.contains(e)).collect(Collectors.toList())

                citiesRequest = region.getCities(conn);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            assertFalse(citiesRequest.isEmpty());
            assertEquals(cityNames.size(), citiesRequest.size());
            assertTrue(Utilities.compareLists(
                    cityNames,
                    citiesRequest.stream().map(Object::toString).collect(Collectors.toList())));
        };

        // Same as continent name
        checkCities.accept(Region.fromName("North America", conn));
        checkCities.accept(Region.fromName("South America", conn));
    }

    @Test
    void getPopulation() throws SQLException {
        final Connection conn = app.getConnectionForIntegrationTesting();

        // total pop
//        SELECT country.Region, SUM(country.Population) AS Total
//        FROM country
//        WHERE Region = "Middle East"
//        GROUP BY country.Region
//        ORDER BY country.Region

        // pop in cities
//        SELECT country.Region, SUM(city.Population) AS CityPop
//        FROM city
//        INNER JOIN country
//        ON country.Code = city.CountryCode
//        GROUP BY country.Region

        PopulationInfo melanesiaInfo = Region.fromName("melanesia", conn).getPopulationInfo(conn);
        assertEquals(6472000,
                melanesiaInfo.total);
        assertEquals(484459,
                melanesiaInfo.inCities);
        assertEquals(6472000 - 484459,
                melanesiaInfo.outsideCities);

        // No population
        assertEquals(0,
                Region.fromName("Micronesia/Caribbean", conn).getPopulationInfo(conn).total);
        assertEquals(0,
                Region.fromName("Antarctica", conn).getPopulationInfo(conn).total);
    }
}
