package com.napier.SET08103;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class for testing App
 */
public class AppTest {
    static App app;

    @BeforeAll
    static void init(){
        app = new App();
    }

    @Test
    void printCountryReportTestNull(){
        app.printCountryReport(null);
    }

    @Test
    void printCountryReportTestEmpty(){
        ArrayList<Country> countries = new ArrayList<>();
        app.printCountryReport(countries);
    }

    @Test
    void printCountryReportContainsNull(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add(null);
        app.printCountryReport(countries);
    }

    @Test
    void printCountryReport(){
        ArrayList<Country> countries = new ArrayList<>();
        Country country = new Country();
        country.code = "GBR";
        country.name = "United Kingdom";
        country.continent = "Europe";
        country.region = "British Islands";
        country.population = 59623400;
        country.capital = "London";
        countries.add(country);
        app.printCountryReport(countries);
    }
}