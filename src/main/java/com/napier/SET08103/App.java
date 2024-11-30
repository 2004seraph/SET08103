package com.napier.SET08103;

import com.napier.SET08103.repl.Repl;
import de.audioattack.io.Console;
import de.audioattack.io.ConsoleCreator;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

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

    public static void main(String[] args) throws SQLException {
        try (App a = new App()) {
            a.connect(
                    Objects.requireNonNullElse(
                            System.getenv("MYSQL_HOST"),
                            "localhost"),
                    Objects.requireNonNullElse(
                            System.getenv("MYSQL_ROOT_PASSWORD"),
                            "default")
                    );

            a.run(args);
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

    public void run(String[] args) {
        if (args.length != 0)
            Repl.parseAndRun(args, con); // command passed via arguments, do not start the prompt
        else {
            // Standard Java Console object is null if used within an IDE setting, this package provides a
            // drop-in fallback.
            Console console = ConsoleCreator.console();

            Repl.printWelcome();

            while (true) {
                String line = console.readLine(" > ");
                String[] subArgs = line.split("\\s+"); // split by any amount of whitespace

                if (subArgs.length == 1 && Objects.equals(subArgs[0], "quit"))
                    break;

                try {
                    Repl.parseAndRun(subArgs, con);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
//        try {
//
//            // Creates an ArrayList of country objects
//            ArrayList<Country> countries = CountryReport.build(
//                    con,
//                    SQLQueries.world_countries_largest_population_to_smallest());
//            // Prints the countries in the ArrayList to console
//            CountryReport.print(countries);
//
//
//            // city report tests
//            ArrayList<City> cities = CityReport.build(
//                    con,
//                    SQLQueries.cities_in_a_country_largest_population_to_smallest("United Kingdom"));
//            CityReport.print(cities, con);
//
//            ArrayList<City> cities2 = CityReport.build(
//                    con,
//                    SQLQueries.cities_in_a_region_largest_population_to_smallest("Western Europe"));
//            CityReport.print(cities2, con);
//
//            ArrayList<City> cities3 = CityReport.build(
//                    con,
//                    SQLQueries.cities_in_world_largest_population_to_smallest());
//            CityReport.print(cities3, con);
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void connect(String dbHost, String dbPassword) throws RuntimeException, SQLException {
        if (!isDriverLoaded())
            throw new RuntimeException("Database driver not loaded");

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

                System.out.println(
                        "Successfully connected to " +
                                con.getMetaData().getDatabaseProductName() +
                                " database: " + con.getCatalog());
                System.out.println();
                break;

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

        if (con == null || !con.isValid(DB_LOGIN_TIMEOUT_SECONDS))
            throw new RuntimeException("Could not connect to database");

        con.setReadOnly(true);
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