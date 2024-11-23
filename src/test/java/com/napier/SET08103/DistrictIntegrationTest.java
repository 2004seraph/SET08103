package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.model.concepts.District;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class DistrictIntegrationTest {
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
    void districtCreate() {
        Connection conn = app.getConnectionForIntegrationTesting();

        BiConsumer<String, String> createValidDistrict = (name, countryCode) -> {
            AtomicReference<District> x = new AtomicReference<>();
            assertAll(() -> x.set(District.fromName(
                    name,
                    Country.fromCountryCode(countryCode, conn),
                    conn)));
            assertEquals(name.toLowerCase(), x.get().toString().toLowerCase());

            assertAll(() -> District.fromName(
                    name,
                    conn));
        };

        Consumer<String> createInvalidDistrict = (name) -> {
            assertThrows(IllegalArgumentException.class, () -> District.fromName(name, conn));
        };
        BiConsumer<String, String> createInvalidDistrictWithCountry = (name, countryCode) -> {
            assertThrows(IllegalArgumentException.class, () -> District.fromName(
                    name,
                    Country.fromCountryCode(countryCode, conn),
                    conn));
        };

        // Valid
        createValidDistrict.accept("kabol", "AFG"); // First entry
        createValidDistrict.accept("Kabol", "AFG"); // Case
        createValidDistrict.accept("KABOL", "AFG");
        createValidDistrict.accept("kaBOL", "AFG");
        createValidDistrict.accept("rafah", "PSE"); // Last entry

        // Invalid
        createInvalidDistrict.accept("Fumbo"); // Non-existent district
        createInvalidDistrictWithCountry.accept("Texas", "AFG");  // Wrong country
        createInvalidDistrictWithCountry.accept("Sam Land", "AFG");  // Non-existent country
    }
}
