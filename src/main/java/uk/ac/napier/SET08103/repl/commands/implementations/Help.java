package uk.ac.napier.SET08103.repl.commands.implementations;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.napier.SET08103.repl.Repl;
import uk.ac.napier.SET08103.repl.commands.ICommand;

import java.sql.Connection;
import java.sql.SQLException;

public final class Help implements ICommand {
    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public Object execute(final CommandLine args, final Connection conn)
            throws SQLException, RuntimeException, ParseException {
        Repl.printTopLevelHelpString();

        return null;
    }
}
