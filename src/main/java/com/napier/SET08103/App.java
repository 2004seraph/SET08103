package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Country;
import com.napier.SET08103.model.concepts.District;

import java.sql.*;
import java.util.*;

/**
 * Allows you to create an App object that you can connect
 * and interact with a database to produce reports
 */
public final class App implements AutoCloseable {
    private static final int DB_MAX_CONN_RETRIES = 10;
    private static final int DB_LOGIN_TIMEOUT_SECONDS = 3;

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

    public static void main(String[] args) {
        try (App a = new App()) {
            a.connect(
                    Objects.requireNonNullElse(
                            System.getenv("MYSQL_HOST"),
                            "localhost"),
                    Objects.requireNonNullElse(
                            System.getenv("MYSQL_ROOT_PASSWORD"),
                            "default")
                    );

            a.run();
        }

        // Connection is automatically closed by try (...) { } calling a.close(); on exit
    }

    //Connection to MySQL database
    private Connection con = null;

    /**
     * Package-private function only allowed for use in JUnit test code.
     * Allows you to create a new App() instance, run its .connect() function,
     * and then use its "con" property in the tests.
     * @return con
     */
    Connection getConnectionForIntegrationTesting() {
        return con;
    }

    public void run() {
        // Creates an ArrayList of country objects
        ArrayList<Country> countries = CountryReport.build(
                con,
                SQLQueries.world_countries_largest_population_to_smallest());
        // Prints the countries in the ArrayList to console
        CountryReport.print(countries);
    }

    public void connect(String dbHost, String dbPassword) throws InternalError {
        if (!isDriverLoaded())
            throw new InternalError("Database driver not loaded");

        Properties connectionProps = new Properties();
        connectionProps.put("user", "root");
        connectionProps.put("password", dbPassword);
        connectionProps.put("useSSL", false);
        connectionProps.put("connectTimeout", DB_LOGIN_TIMEOUT_SECONDS * 1000);

        DriverManager.setLoginTimeout(DB_LOGIN_TIMEOUT_SECONDS);

        for (int i = 0; i < DB_MAX_CONN_RETRIES; i++) {
            try {
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + dbHost + ":3306/world",
                        connectionProps);

                System.out.println("Successfully connected to database");
                return;

            } catch (SQLException e) {
                System.err.println(
                        "[Attempt (" + (i + 1) + "/" + DB_MAX_CONN_RETRIES + ")] " +
                                "Failed to connect to database:");
                System.err.println(e.getMessage() + "\n");
            }
            try {
                // Wait
                Thread.sleep(DB_LOGIN_TIMEOUT_SECONDS * 1000);
            } catch (InterruptedException ie) {
                System.err.println("Wait thread interrupted, fatal error");
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
}