package uk.ac.napier.SET08103.model;

import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.District;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public final class DistrictIntegrationTest extends AbstractIntegrationTest {

    static final List<String> texasCities = List.of(
            "Houston",
            "Dallas",
            "San Antonio",
            "Austin",
            "El Paso",
            "Fort Worth",
            "Arlington",
            "Corpus Christi",
            "Plano",
            "Garland",
            "Lubbock",
            "Irving",
            "Laredo",
            "Amarillo",
            "Brownsville",
            "Pasadena",
            "Grand Prairie",
            "Mesquite",
            "Abilene",
            "Beaumont",
            "Waco",
            "Carrollton",
            "McAllen",
            "Wichita Falls",
            "Midland",
            "Odessa");

    @SuppressWarnings({"SpellCheckingInspection", "ExtractMethodRecommender"})
    @Test
    public void districtCreate() {
        final Connection conn = getAppDatabaseConnection();

        final String NON_EQUAL_DISTRICT = "Texas";
        final BiConsumer<String, String> createValidDistrict = (name, countryCode) -> {
            AtomicReference<District> x = new AtomicReference<>();
            assertAll(() -> x.set(District.fromName(
                    name,
                    Country.fromCountryCode(countryCode, conn),
                    conn)));
            assertEquals(name.toLowerCase(), x.get().toString().toLowerCase());

            assertAll(() -> District.fromName(
                    name,
                    conn));

            // getValue() on IFieldEnum
            assertEquals(name.toLowerCase(), x.get().getValue().toLowerCase());

            // Testing positive .equals()
            try {
                assertEquals(x.get(), District.fromName(name, countryCode, conn));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Testing fromName without an explicit country as well as negative .equals()
            try {
                assertNotEquals(x.get(), District.fromName(NON_EQUAL_DISTRICT, conn));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        final Consumer<String> createInvalidDistrict = (name) ->
                assertThrows(IllegalArgumentException.class, () -> District.fromName(name, conn));

        final BiConsumer<String, String> createInvalidDistrictWithCountry = (name, countryCode) ->
                assertThrows(IllegalArgumentException.class, () -> District.fromName(
                        name,
                        Country.fromCountryCode(countryCode, conn),
                        conn));

        // Valid
        createValidDistrict.accept("kabol", "AFG"); // First entry
        createValidDistrict.accept("Kabol", "AFG"); // Case
        createValidDistrict.accept("KABOL", "AFG");
        createValidDistrict.accept("kaBOL", "AFG");
        createValidDistrict.accept("rafah", "PSE"); // Last entry

        // Null District enum value
        try {
            assertNull(District.fromName(District.nullFieldValue, conn));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Invalid
        createInvalidDistrict.accept("Fumbo"); // Non-existent district
        createInvalidDistrictWithCountry.accept("Texas", "AFG");  // Wrong country
        createInvalidDistrictWithCountry.accept("Sam Land", "AFG");  // Non-existent country
    }

    @Test
    public void zoneInfo() throws SQLException {
        final Connection conn = getAppDatabaseConnection();

        // Outer zone
        final District texas = District.fromName("Texas", conn);
        assert texas != null;
        final Country usa = Country.fromCountryCode("USA", conn);
        assertEquals(usa, texas.getOuterZone());
        assertEquals(usa.toString(), texas.getOuterZone().toString());
        assertNotEquals(Country.fromCountryCode("AFG", conn), texas.getOuterZone());

        // Inner zones
        final List<IZone> texasCitiesRequest = texas.getInnerZones(conn);
        // In case the request returns duplicated elements that will get hidden by the set conversion
        assertEquals(texasCitiesRequest.size(), texasCities.size());
        final HashSet<String> texasCitiesRequestAsUniqueStrings = texasCitiesRequest
                .stream()
                .map(Object::toString).collect(Collectors.toCollection(HashSet::new));

        // Check if all cities are present, regardless of ordering
        assertEquals(
                new HashSet<>(texasCities),
                texasCitiesRequestAsUniqueStrings
        );

        // getCities(), should be the same as getInnerZones()
        assertEquals(
                new HashSet<>(texasCities),
                texas.getCities(conn)
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toCollection(HashSet::new)));
    }

    @Test
    public void getTotalPopulation() {
        final Connection conn = getAppDatabaseConnection();

        final BiFunction<String, String, Long> getDistrictPopulation = (cc, name) -> {
            try {
                return District.fromName(name, cc, conn).getTotalPopulation(conn);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };

        assertEquals(9208281, getDistrictPopulation.apply("USA", "Texas").intValue());
        assertEquals(8958085, getDistrictPopulation.apply("USA", "New York").intValue());
        // Unicode
        assertEquals(26316966, getDistrictPopulation.apply("BRA", "São Paulo").intValue());
    }
}
