package uk.ac.napier.SET08103.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.Testing;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.Region;
import uk.ac.napier.SET08103.model.concepts.types.PopulationInfo;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CountryIntegrationTest extends AbstractIntegrationTest {

    final List<String> districtsOfAustralia = List.of(
            "New South Wales",
            "Victoria",
            "Queensland",
            "West Australia",
            "South Australia",
            "Capital Region",
            "Tasmania"
    );

    @Test
    void countryCreate() throws SQLException {
        Connection conn = getAppDatabaseConnection();

        BiFunction<String, String, Country> createCountry = (cc, name) -> {
            // fromId
            final AtomicReference<Country> fromCC = new AtomicReference<>();
            assertAll(() -> fromCC.set(Country.fromCountryCode(cc, conn)));

            // fromName
            final AtomicReference<Country> fromName = new AtomicReference<>();
            assertAll(() -> fromName.set(Country.fromName(name, conn)));

            // Same?
            Assertions.assertEquals(fromName.get(), fromCC.get());

            return fromCC.get();
        };

        Assertions.assertEquals(City.fromId(3813, conn), createCountry.apply("USA", "United States").capital);
        Assertions.assertEquals(City.fromId(4068, conn), createCountry.apply("ZWE", "Zimbabwe").capital);

        // NULL capital
        Assertions.assertNull(createCountry.apply("ATA", "Antarctica").capital);
        // Partial name
        Assertions.assertNull(createCountry.apply("UMI", "United States Minor").capital);

        // Misspelling
        //noinspection SpellCheckingInspection
        assertThrows(IllegalArgumentException.class, () ->
                Country.fromCountryCode("AUSS", conn));
        assertThrows(IllegalArgumentException.class, () ->
                Country.fromCountryCode("AU", conn));

        // non-existent
        assertThrows(IllegalArgumentException.class, () ->
                Country.fromCountryCode("SAM", conn));

        // Invalid
        assertThrows(IllegalArgumentException.class, () ->
                Country.fromCountryCode("", conn));
    }

    @Test
    void zoneInfo() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        // getOuterZone() : Austria in Western Europe
        final Country austria = Country.fromCountryCode("AUT", conn);
        Assertions.assertEquals(Region.fromName("Western Europe", conn), austria.getOuterZone());
        Assertions.assertNotEquals(Region.fromName("Caribbean", conn), austria.getOuterZone());

        // getInnerZones() : States of Australia
        final Country australia = Country.fromCountryCode("AUS", conn);
        final List<IZone> australiaDistrictsRequest = australia.getInnerZones(conn);
        // In case the request returns duplicated elements that will get hidden by the set conversion
        Assertions.assertEquals(australiaDistrictsRequest.size(), districtsOfAustralia.size());
        final HashSet<String> australiaDistrictsRequestAsUniqueStrings = australiaDistrictsRequest
                .stream()
                .map(Object::toString).collect(Collectors.toCollection(HashSet::new));
        // Check if all states are present, regardless of ordering
        Assertions.assertEquals(new HashSet<>(districtsOfAustralia), australiaDistrictsRequestAsUniqueStrings);

        // getCities()
        Consumer<Country> checkCities = (country) -> {
            List<String> cityNames = new ArrayList<>();
            List<City> citiesRequest;

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT " + City.NAME + " FROM " + City.TABLE +
                    " WHERE " + City.COUNTRY_CODE + " = ?"
                )) {
                stmt.setString(1, country.countryCode);

                try (ResultSet res = stmt.executeQuery()) {
                    while (res.next())
                        cityNames.add(res.getString(City.NAME));
                }

                citiesRequest = country.getCities(conn);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Assertions.assertFalse(citiesRequest.isEmpty());
            Assertions.assertEquals(cityNames.size(), citiesRequest.size());
            Assertions.assertTrue(Testing.compareLists(
                    cityNames,
                    citiesRequest.stream().map(Object::toString).collect(Collectors.toList())));
        };

        // Unique city names
        checkCities.accept(australia);
        checkCities.accept(austria);

        // Duplicate city names
        checkCities.accept(Country.fromCountryCode("USA", conn));

        // Other
        // LKA has district names in common with PRY, this tests that distinction
        checkCities.accept(Country.fromCountryCode("LKA", conn));
        checkCities.accept(Country.fromCountryCode("PRY", conn));

        checkCities.accept(Country.fromCountryCode("ARG", conn));
    }

    @Test
    void getPopulation() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        PopulationInfo usaInfo = Country.fromCountryCode("USA", conn).getPopulationInfo(conn);
        Assertions.assertEquals(278357000, usaInfo.total);
        Assertions.assertEquals(78625774, usaInfo.inCities);
        Assertions.assertEquals(278357000 - 78625774, usaInfo.outsideCities);

        // No population
        Assertions.assertEquals(0, Country.fromCountryCode("ATA", conn).getPopulationInfo(conn).total);

        // null districts
        PopulationInfo arubaInfo = Country.fromCountryCode("ABW", conn).getPopulationInfo(conn);
        Assertions.assertEquals(103000, arubaInfo.total);
        Assertions.assertEquals(29034, arubaInfo.inCities);
    }
}
