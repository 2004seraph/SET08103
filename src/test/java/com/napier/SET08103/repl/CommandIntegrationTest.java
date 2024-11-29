package com.napier.SET08103.repl;

import com.napier.SET08103.AbstractIntegrationTest;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class CommandIntegrationTest extends AbstractIntegrationTest {

    @Test
    void populationOf() throws SQLException, ParseException {
        Connection conn = getAppDatabaseConnection();

        var x = Repl.parseAndRun(
                conn,
                "total", "--in", "city:london"
        );

        assertEquals(((Integer) Repl.parseAndRun(
                conn,
                "total", "--in", "city:london"
        )).intValue(), 7285000);
    }
}
