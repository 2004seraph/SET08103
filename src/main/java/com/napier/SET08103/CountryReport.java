package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public final class CountryReport {

    private CountryReport() { }

    /**
     * Executes an SQL query and extracts the results into country objects,
     * returning an ArrayList of country objects
     * @param strSelect
     * @return
     */
    public static ArrayList<Country> build(Connection con, String strSelect)
    {
        try (Statement stmt = con.createStatement())
        {
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);

            // Extract country information
            ArrayList<Country> countries = new ArrayList<>();
            while (rset.next())
            {
                Country country = Country.fromCountryCode(rset.getString("code"), con);
                country.name = rset.getString("name");
                country.continent = rset.getString("continent");
                country.region = rset.getString("region");
                country.population = Integer.parseInt(rset.getString("population"));
                country.capital = rset.getString("capital");

                countries.add(country);
            }
            return countries;
        }
        catch (Exception e)
        {
//            System.out.println(e.getMessage());
//            System.out.println("Failed to get country details");
//            return null;
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints country objects to console
     * @param countries
     */
    public static void print(ArrayList<Country> countries){
        if (countries == null){
            System.out.println("No countries");
            return;
        }

        // Print header
        System.out.printf(
                "%-4s %-45s %-14s %-26s %-11s %-9s%n",
                "Code", "Name", "Continent", "Region", "Population", "Capital");

        // Prints each country
        for (Country country : countries){
            if (country == null) continue;

            String country_string = String.format(
                    "%-4s %-45s %-14s %-26s %-11s %-9s",
                    country.getPrimaryKey(), country.name, country.continent, country.region, country.population, country.capital);
            System.out.println(country_string);
        }
        System.out.println();
    }
}
