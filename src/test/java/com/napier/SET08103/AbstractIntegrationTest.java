package com.napier.SET08103;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Common startup and tear down methods relating to the database connection, as well as a single
 * source of package-private main package data.
 */
public abstract class AbstractIntegrationTest {
    protected static final App app = new App();

    @BeforeAll
    protected static void connectToDatabase() throws SQLException {
        app.connect(
                Objects.requireNonNullElse(
                        System.getenv(Testing.MYSQL_HOST_ENVAR),
                        Testing.MYSQL_HOST_ENVAR_DEFAULT),
                Objects.requireNonNullElse(
                        System.getenv(Testing.MYSQL_ROOT_PASSWORD_ENVAR),
                        Testing.MYSQL_ROOT_PASSWORD_DEFAULT)
        );
    }

    protected static Connection getAppDatabaseConnection() {
        return app.getConnectionForIntegrationTesting();
    }

    @AfterAll
    static void deInit() {
        app.close();
    }
}
