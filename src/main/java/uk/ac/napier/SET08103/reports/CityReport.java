package uk.ac.napier.SET08103.reports;

import uk.ac.napier.SET08103.model.concepts.City;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public final class CityReport {

    private CityReport() { }

    /**
     * Executes an SQL query and extracts the results into city objects,
     * returning an ArrayList of city objects
     */
    @SuppressWarnings("unused")
    public static ArrayList<City> build(Connection con, String strSelect)
    {
        try (Statement stmt = con.createStatement())
        {
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);

            // Extract city information
            ArrayList<City> cities = new ArrayList<>();
            while (rset.next())
            {
                City city = City.fromId(Integer.parseInt(rset.getString("id")), con);
                cities.add(city);
            }

            return cities;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints city objects to console
     */
    public static void print(ArrayList<City> cities, Connection conn) {
        if (cities == null){
            System.out.println("No cities");
            return;
        }

        // Print header
        System.out.printf(
                "%-36s %-46s %-24s %-10s%n",
                "Name", "Country", "District", "Population");

        // Prints each city
        for (City city : cities){
            if (city == null) continue;

            String district = (city.getDistrict() == null) ? "-" : city.getDistrict().toString();

            String city_string = String.format(
                    "%-36s %-46s %-24s %-10s",
                    city, city.getCountry(), district, city.getTotalPopulation(conn));
            System.out.println(city_string);
        }
        System.out.println();
    }
}
