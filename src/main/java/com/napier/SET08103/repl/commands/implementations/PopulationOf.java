package com.napier.SET08103.repl.commands.implementations;

import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

import static com.napier.SET08103.repl.Repl.parseZoneReference;

public class PopulationOf implements ICommand {
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
    public Object execute(CommandLine args, Connection conn) throws SQLException, RuntimeException, ParseException {
        IZone target = parseZoneReference(args.getOptionProperties("in"), conn);
        long population = target.getTotalPopulation(conn);
        System.out.println("Population of " + target.toString() + ": " + population);

        return population;
    }
}
