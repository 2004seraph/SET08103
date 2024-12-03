package uk.ac.napier.SET08103;

import org.junit.jupiter.api.Test;
import uk.ac.napier.SET08103.repl.commands.Command;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.junit.jupiter.api.Assertions.*;

public final class AppIntegrationTest {

    @Test
    void driverCheck() throws SQLException {
        assertTrue(App.isDriverLoaded());

        Driver mysqlDriver = null;
        {
            final Enumeration<Driver> list = DriverManager.getDrivers();
            while (list.hasMoreElements()) {
                final Driver driver = list.nextElement();
                if (driver.toString().contains("mysql")) {
                    mysqlDriver = driver;
                }
            }
            assert mysqlDriver != null;

            DriverManager.deregisterDriver(mysqlDriver);
        }

        assertFalse(App.isDriverLoaded());

        DriverManager.registerDriver(mysqlDriver);

        assertTrue(App.isDriverLoaded());
    }

    @Test
    void cannotConnect() throws Exception {
        withEnvironmentVariable("MYSQL_ROOT_PASSWORD", "chungus")
                .execute(() -> assertThrows(
                        RuntimeException.class,
                        () -> App.main(new String[]{ })));
    }

    @Test
    void runCLI() throws Exception {
        assertAll(() ->
                App.main(new String[]{ Command.LEADERBOARD.name(), "--of", "countries", "--in", "continent:europe" }));
    }

    @Test
    void runInteractive_nothing() throws Exception {
        withTextFromSystemIn()
                .execute(() -> assertAll(() ->
                        App.main(new String[]{ })));
    }

    @Test
    void runInteractive_command() throws Exception {
        withTextFromSystemIn("leaderboard")
                .execute(() -> assertAll(() ->
                        App.main(new String[]{ })));
    }
}
