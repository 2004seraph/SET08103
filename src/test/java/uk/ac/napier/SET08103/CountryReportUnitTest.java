package uk.ac.napier.SET08103;

import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.reports.CountryReport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Class for testing App
 */
public final class CountryReportUnitTest {

    @Test
    public void printCountryReportTestNull(){
        CountryReport.print(null);
    }

    @Test
    public void printCountryReportTestEmpty(){
        ArrayList<Country> countries = new ArrayList<>();
        CountryReport.print(countries);
    }

    @Test
    public void printCountryReportContainsNull(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add(null);
        CountryReport.print(countries);
    }
}