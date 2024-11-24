package com.napier.SET08103;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Objects;

public abstract class AbstractIntegrationTest {
    static App app;

    @BeforeAll
    static void init() {
        app = new App();

        app.connect(
                Objects.requireNonNullElse(
                        System.getenv(Utilities.MYSQL_HOST_ENVAR),
                        Utilities.MYSQL_HOST_ENVAR_DEFAULT),
                Objects.requireNonNullElse(
                        System.getenv(Utilities.MYSQL_ROOT_PASSWORD_ENVAR),
                        Utilities.MYSQL_ROOT_PASSWORD_DEFAULT)
        );
    }

    @AfterAll
    static void deInit() {
        app.close();
    }
}
