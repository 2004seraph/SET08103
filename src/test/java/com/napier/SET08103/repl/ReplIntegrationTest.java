package com.napier.SET08103.repl;

import com.napier.SET08103.AbstractIntegrationTest;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ReplIntegrationTest extends AbstractIntegrationTest {

    @Test
    void parseZoneReference() throws ParseException, SQLException {
        Connection conn = getAppDatabaseConnection();

        CommandLine args = parseCommandLine(new TestCommand(), new String[]{ "--in", "continent:Europe" });
        Properties p = args.getOptionProperties("in");
        IZone aggregateArea = (AbstractZone) Repl.parseZoneReference(p, conn);
    }

    private class TestCommand implements ICommand {
        @Override
        public Options getOptions() {
            return new Options()
                    // --in continent:Europe
                    .addOption(
                            Option.builder("i")
                                    .longOpt("in")
                                    .hasArgs()
                                    .numberOfArgs(2)
                                    .argName("<world/continent/region>:[]")
                                    .valueSeparator(':')
                                    .build()
                    );
        }

        @Override
        public void execute(CommandLine args, Connection conn) throws SQLException, InternalError, ParseException { }
    }

    private CommandLine parseCommandLine(ICommand command, String[] args) throws ParseException {
        return new DefaultParser().parse(
                command.getOptions(),
                args,
                false // true = throw, false so it ignores the first arg
        );
    }
}
