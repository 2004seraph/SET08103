package com.napier.SET08103;

import com.napier.SET08103.model.concepts.Region;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.*;

public final class RegionIntegrationTest {
    static App app;

    @BeforeAll
    static void init() {
        app = new App();

        app.connect(
                Objects.requireNonNullElse(
                        System.getenv(Constants.MYSQL_HOST_ENVAR),
                        Constants.MYSQL_HOST_ENVAR_DEFAULT),
                Objects.requireNonNullElse(
                        System.getenv(Constants.MYSQL_ROOT_PASSWORD_ENVAR),
                        Constants.MYSQL_ROOT_PASSWORD_DEFAULT)
        );
    }

    @AfterAll
    static void deInit() {
        app.close();
    }


    //region RegionTests

//    @Test
//    void regionCreate() {
//        Connection conn = app.getConnectionForIntegrationTesting();
//
//        assertAll(() -> Region.fromName(
//                "",
//                conn));
//    }

    //endregion
}
