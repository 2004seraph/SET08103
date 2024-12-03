package uk.ac.napier.SET08103.reports;

import uk.ac.napier.SET08103.model.concepts.City;

import java.sql.Connection;
import java.util.ArrayList;

public final class CapitalReport {
    /**
     * Outputs a list of Capitals in the table format required for a Capital report
     * @param cities Capitals
     */
    public static void print(final ArrayList<City> cities, final Connection conn) {
        assert cities != null;

        // Print header
        System.out.printf(
                "%-36s %-46s %-10s%n",
                "Name", "Country", "Population");

        // Prints each city
        for (final City city : cities){
            if (city == null) continue;

            final String city_string = String.format(
                    "%-36s %-46s %,10d",
                    city, city.getCountry(), city.getTotalPopulation(conn));
            System.out.println(city_string);
        }
        System.out.println();
    }
}
