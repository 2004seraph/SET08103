package com.napier.SET08103.repl.commands.implementations;

import com.napier.SET08103.model.concepts.World;
import com.napier.SET08103.model.concepts.types.PopulationInfo;
import com.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.napier.SET08103.repl.Repl.parseZoneReference;

/**
 * For generating population reports
 */
public final class RichPopulationInfo implements ICommand {
    @Override
    public Options getOptions() {
        return new Options()
                // --in continent:Europe
                .addOption(
                        Option.builder("i")
                                .longOpt("in")
                                .hasArgs()
                                .numberOfArgs(2)
                                .argName("<world/continent/region/country/district/city>:[name or ID]")
                                .valueSeparator(':')
                                .build()
                )
                // --of continents
                .addOption(
                        Option.builder("o")
                                .longOpt("of")
                                .hasArg()
                                .argName("continents/regions/countries")
                                .build()
        );
    }

    @Override
    public Object execute(CommandLine args, Connection conn) throws SQLException, RuntimeException {
        if (args.hasOption("in") && args.hasOption("of"))
            throw new IllegalArgumentException("--of and --in are mutually exclusive for this command");

        if (args.hasOption("in")) {
            IZone target = parseZoneReference(args.getOptionProperties("in"), conn);

            if (!(target instanceof IDistributedPopulation))
                throw new IllegalArgumentException("Selected zone type does not have rich population data");

            PopulationInfo populationInfo =
                    ((IDistributedPopulation) target).getPopulationInfo(conn);

            PopulationInfo.printHeaders();
            populationInfo.print(conn);

            return populationInfo;
        }
        if (args.hasOption("of")) {
            Zone areaType = Zone.valueOf(args.getOptionValue("of").toUpperCase());

            if (!EnumSet.of(Zone.CONTINENTS, Zone.REGIONS, Zone.COUNTRIES).contains(areaType))
                throw new IllegalArgumentException("--of may only be one of: continents, regions, countries");

            int levelsDown = Integer.numberOfLeadingZeros(areaType.getSizeRank()) -
                    Integer.numberOfLeadingZeros(Zone.WORLD.getSizeRank());

            List<IZone> expansion = World.INSTANCE.getInnerZones(levelsDown, conn);

            PopulationInfo.printHeaders();
            return expansion.stream().sorted().map(e -> {
                try {
                    PopulationInfo r = ((IDistributedPopulation)e).getPopulationInfo(conn);
                    r.print(conn);
                    return r;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.toList());
        }

        throw new IllegalArgumentException("Either an --of and --in parameter must be supplied");
    }
}
