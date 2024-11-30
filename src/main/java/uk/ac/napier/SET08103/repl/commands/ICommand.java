package uk.ac.napier.SET08103.repl.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Basic command interface for the Repl class
 */
public interface ICommand {
    Options getOptions();

    // This has a catch-all return value purely for testing, as I know what to cast to based
    // on the specific test
    Object execute(CommandLine args, Connection conn) throws SQLException, RuntimeException, ParseException;
}
