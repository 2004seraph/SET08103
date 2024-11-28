package com.napier.SET08103.repl.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Basic command interface for the Repl class
 */
public interface ICommand {
    public Options getOptions();

    void execute(CommandLine args, Connection conn) throws SQLException, InternalError, ParseException;
}
