package uk.ac.napier.SET08103.reports;

import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.AbstractIntegrationTest;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.repl.commands.SQLQueries;

import java.sql.Connection;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;

public final class ReportIntegrationTest extends AbstractIntegrationTest {

    @Test
    void buildCountryReport() {
        Connection con = getAppDatabaseConnection();

        ArrayList<Country> countries = CountryReport.build(
                con,
                SQLQueries.world_countries_largest_population_to_smallest());

        assertFalse(countries.isEmpty());
    }

    @Test
    void buildCityReport() {
        Connection con = getAppDatabaseConnection();

        ArrayList<City> cities = CityReport.build(
                con,
                SQLQueries.cities_in_a_country_largest_population_to_smallest("United Kingdom"));
        assertFalse(cities.isEmpty());

        ArrayList<City> cities2 = CityReport.build(
                con,
                SQLQueries.cities_in_a_region_largest_population_to_smallest("Western Europe"));
        assertFalse(cities2.isEmpty());

        ArrayList<City> cities3 = CityReport.build(
                con,
                SQLQueries.cities_in_world_largest_population_to_smallest());
        assertFalse(cities3.isEmpty());
    }
}
