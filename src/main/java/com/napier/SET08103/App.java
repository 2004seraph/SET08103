package com.napier.SET08103;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Allows you to create an App object that you can connect
 * and interact with a database to produce reports
 */
public class App implements AutoCloseable {
    private static final int MAX_DB_CONN_RETRIES = 3;
    private static final int DB_LOGIN_TIMEOUT_SECONDS = 2;

    //Connection to MySQL database
    private Connection con = null;

    public static void main(String[] args) {
        try (App a = new App()) {
            a.connect(System.getenv("MYSQL_ROOT_PASSWORD"));

            // Creates an ArrayList of country objects
            ArrayList<Country> countries = a.countryReport(
                    SQLQueries.world_countries_largest_population_to_smallest());
            // Prints the countries in the ArrayList to console
            a.printCountryReport(countries);
        }

        // Connection is automatically closed
    }

    public void connect(String dbPassword) throws InternalError {
        if (!isDriverLoaded())
            throw new InternalError("Database driver not loaded");

        Properties connectionProps = new Properties();
        connectionProps.put("user", "root");
        connectionProps.put("password", dbPassword);
        connectionProps.put("useSSL", false);

        connectionProps.put("connectTimeout", DB_LOGIN_TIMEOUT_SECONDS * 1000);
        DriverManager.setLoginTimeout(DB_LOGIN_TIMEOUT_SECONDS);

        for (int i = 0; i < MAX_DB_CONN_RETRIES; i++) {
            try {
                con = DriverManager.getConnection(
                        "jdbc:mysql://0.0.0.0:3306/world",
                        connectionProps);

                System.out.println("Successfully connected to database");
                return;

            } catch (SQLException e) {
                System.err.println(
                        "[Attempt (" + (i + 1) + "/" + MAX_DB_CONN_RETRIES + ")] " +
                                "Failed to connect to database:");
                System.err.println(e.getMessage() + "\n");
            }
        }

        throw new InternalError("Could not connect to database");
    }

    /**
     * Disconnect from the MySQL database.
     */
    @Override
    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                System.err.println("Error closing connection to database");
            }
        }
    }

    public static Boolean isDriverLoaded()  {
        Enumeration<Driver> list = DriverManager.getDrivers();
        while (list.hasMoreElements()) {
            Driver driver = list.nextElement();
            if (driver.toString().contains("mysql")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Executes an SQL query and extracts the results into country objects,
     * returning an ArrayList of country objects
     * @param strSelect
     * @return
     */
    public ArrayList<Country> countryReport(String strSelect)
    {
        try (Statement stmt = con.createStatement())
        {
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
        System.out.printf(
                "%-4s %-45s %-14s %-26s %-11s %-9s%n",
                "Code", "Name", "Continent", "Region", "Population", "Capital");

        // Prints each country
        for (Country country : countries){
            if (country == null) continue;

            String country_string = String.format(
                    "%-4s %-45s %-14s %-26s %-11s %-9s",
                    country.code, country.name, country.continent, country.region, country.population, country.capital);
            System.out.println(country_string);
        }
    }
}