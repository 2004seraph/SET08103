package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Class for testing App
 */
public final class AppUnitTest {
    static App app;

    @BeforeAll
    static void init(){
        app = new App();
    }

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

    @Test
    void printCountryReport(){
//        ArrayList<Country> countries = new ArrayList<>();
//        Country country = Country.fromCountryCode("GBR", );
//        country.name = "United Kingdom";
//        country.continent = "Europe";
//        country.region = "British Islands";
//        country.population = 59623400;
//        country.capital = "London";
//        countries.add(country);
//        CountryReport.print(countries);
    }
}