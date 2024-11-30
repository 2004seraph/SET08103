package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.reports.CountryReport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Class for testing App
 */
public final class CountryReportUnitTest {

    @Test
    void printCountryReportTestNull(){
        CountryReport.print(null);
    }

    @Test
    void printCountryReportTestEmpty(){
        ArrayList<Country> countries = new ArrayList<>();
        CountryReport.print(countries);
    }

    @Test
    void printCountryReportContainsNull(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add(null);
        CountryReport.print(countries);
    }
}