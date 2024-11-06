package com.napier.SET08103;

import java.sql.*;
import java.util.ArrayList;

/**
 * Allows you to create an App object that you can connect and interact with a database to produce reports
 */
public class App {
    public static void main(String[] args) {
        // Create new Application
        App a = new App();

        // Connect to database
        a.connect();

        // Creates an ArrayList of country objects
        ArrayList<Country> countries = a.countryReport(SQLQueries.world_countries_largest_population_to_smallest());

        // Prints the countries in the ArrayList to console
        a.printCountryReport(countries);

        // Disconnect from database
        a.disconnect();
    }


    //Connection to MySQL database
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect() {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 20;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://db:3306/world?useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Executes an SQL query and extracts the results into country objects, returning an ArrayList of country objects
     * @param strSelect
     * @return
     */
    public ArrayList<Country> countryReport(String strSelect)
    {
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);

            // Extract country information
            ArrayList<Country> countries = new ArrayList<>();
            while (rset.next())
            {
                Country country = new Country();

                country.code = rset.getString("code");
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
            System.out.println(e.getMessage());
            System.out.println("Failed to get country details");
            return null;
        }
    }

    /**
     * Prints country objects to console
     * @param countries
     */
    public void printCountryReport(ArrayList<Country> countries){
        if (countries == null){
            System.out.println("No countries");
            return;
        }

        // Print header
        System.out.println(String.format("%-4s %-45s %-14s %-26s %-11s %-9s", "Code", "Name", "Continent", "Region", "Population", "Capital"));

        // Prints each country
        for (Country country : countries){
            if (country == null) continue;

            String country_string = String.format("%-4s %-45s %-14s %-26s %-11s %-9s", country.code, country.name, country.continent, country.region, country.population, country.capital);
            System.out.println(country_string);
        }
    }
}