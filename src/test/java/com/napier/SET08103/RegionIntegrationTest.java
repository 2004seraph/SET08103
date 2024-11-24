package com.napier.SET08103;

import com.napier.SET08103.model.concepts.City;
import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.model.concepts.Region;
import com.napier.SET08103.model.concepts.zone.IZone;
import org.junit.jupiter.api.Disabled;
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
                    "SELECT " + City.nameField +
                            " FROM " + City.table +
                            " INNER JOIN " + Country.table +
                            " ON " + Country.primaryKeyField +
                            " = " + City.countryCodeField +
                            " WHERE " + Country.regionField + " = ?"
            )) {
                stmt.setString(1, region.toString());

                try (ResultSet res = stmt.executeQuery()) {
                    while (res.next())
                        cityNames.add(res.getString(City.nameField));
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
}
