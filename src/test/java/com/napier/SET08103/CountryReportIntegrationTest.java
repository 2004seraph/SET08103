package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.reports.CountryReport;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

public final class CountryReportIntegrationTest extends AbstractIntegrationTest {
    @Test
    void printCountryReport() throws SQLException {
        ArrayList<Country> countries = new ArrayList<>();
        Country country = Country.fromCountryCode("GBR", app.getConnectionForIntegrationTesting());
        countries.add(country);
        CountryReport.print(countries);
    }
}
