package uk.ac.napier.SET08103.repl.commands.implementations;

import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.sql.Connection;
import java.sql.SQLException;

import static uk.ac.napier.SET08103.repl.Repl.parseZoneReference;

/**
 * For getting population totals
 */
public final class PopulationOf implements ICommand {
    @Override
    public Options getOptions() {
        return new Options()
                // --in continent:Europe
                .addOption(
                        Option.builder("i")
                                .longOpt("in")
                                .required()
                                .hasArgs()
                                .numberOfArgs(2)
                                .argName("<world/continent/region/country/district/city>:[name or ID]")
                                .valueSeparator(':')
                                .build()
                );
    }

    @Override
    public Object execute(final CommandLine args, final Connection conn) throws SQLException, RuntimeException {
        final IZone target = parseZoneReference(args.getOptionProperties("in"), conn);
        final long population = target.getTotalPopulation(conn);

        System.out.println("Population of " + target + ": " + population);

        return population;
    }
}
