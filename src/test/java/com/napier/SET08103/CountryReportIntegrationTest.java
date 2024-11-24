package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

public final class CountryReportIntegrationTest extends AbstractIntegrationTest {
    @Test
    void printCountryReport() throws SQLException {
        ArrayList<Country> countries = new ArrayList<>();
        Country country = Country.fromCountryCode("GBR", app.getConnectionForIntegrationTesting());
        country.name = "United Kingdom";
        country.population = 59623400;
        country.capital = "London";
        countries.add(country);
        CountryReport.print(countries);
    }
}
