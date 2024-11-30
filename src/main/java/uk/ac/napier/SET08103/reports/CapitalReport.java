package uk.ac.napier.SET08103.reports;

import uk.ac.napier.SET08103.model.concepts.City;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public final class CapitalReport {
    public static void print(ArrayList<City> cities, Connection conn) throws SQLException {
        if (cities == null){
            System.out.println("No cities");
            return;
        }

        // Print header
        System.out.printf(
                "%-36s %-46s %-10s%n",
                "Name", "Country", "Population");

        // Prints each city
        for (City city : cities){
            if (city == null) continue;

            String city_string = String.format(
                    "%-36s %-46s %-10s",
                    city, city.getCountry(), city.getTotalPopulation(conn));
            System.out.println(city_string);
        }
        System.out.println();
    }
}
