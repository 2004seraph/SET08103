package com.napier.SET08103.repl.commands.implementations;

import com.napier.SET08103.model.concepts.types.PopulationInfo;
import com.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

import static com.napier.SET08103.repl.Repl.parseZoneReference;

public final class RichPopulationInfo extends PopulationOf implements ICommand {
    // Same args as PopulationOf, but with a different output format

    @Override
    public Object execute(CommandLine args, Connection conn) throws SQLException, RuntimeException, ParseException {
        IZone target = parseZoneReference(args.getOptionProperties("in"), conn);

        if (!(target instanceof IDistributedPopulation))
            throw new IllegalArgumentException("Selected zone type does not have rich population data");

        PopulationInfo populationInfo =
                ((IDistributedPopulation) target).getPopulationInfo(conn);

        populationInfo.print(conn);

        return populationInfo;
    }
}
