package com.napier.SET08103.repl;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.repl.commands.Command;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class CommandIntegrationTest extends AbstractIntegrationTest {

    @Test
    void populationOf() throws SQLException, ParseException {
        Connection conn = getAppDatabaseConnection();
        assertEquals(((Long) Repl.parseAndRun(
                conn,
                Command.TOTAL.name(), "--in", "city:london"
        )).longValue(), 7285000);
    }
}
