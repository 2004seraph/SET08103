package com.napier.SET08103.repl.commands.implementations;

import com.napier.SET08103.model.concepts.City;
import com.napier.SET08103.model.concepts.World;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.repl.commands.ICommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.napier.SET08103.repl.Repl.parseZoneReference;

public final class Leaderboard implements ICommand {

    @Override
    public Options getOptions() {
        return new Options()
                // --of capitals
                .addOption(
                        Option.builder("o")
                                .longOpt("of")
                                .required()
                                .hasArg()
                                .argName("cities/countries/capitals")
                                .build()
                )
                // --in continent:Europe
                .addOption(
                        Option.builder("i")
                                .longOpt("in")
                                .hasArgs()
                                .numberOfArgs(2)
                                .argName("<world/continent/region>:[]")
                                .valueSeparator(':')
                                .build()
                )
                // --top 10
                .addOption(
                        Option.builder("t")
                                .longOpt("top")
                                .hasArg()
                                .type(Integer.class)
                                .argName("N")
                                .build()
                );
    }

    @Override
    public Object execute(CommandLine args, Connection conn) throws SQLException, InternalError, ParseException {
        assert args.getOptionValue("of") != null;

        // --of
        Zone areaType;

        // --in
        Zone aggregateAreaType = Zone.WORLD;
        AbstractZone aggregateArea = World.INSTANCE;

        // --top
        int top = -1;

        try {
            areaType = Zone.valueOf(args.getOptionValue("of").toUpperCase());
            if (areaType.getSizeRank() > Zone.COUNTRIES.getSizeRank())
                throw new InternalError("Invalid combination of --of and --in parameters");
        } catch (IllegalArgumentException e) {
            throw new InternalError(e);
        }

        if (args.hasOption("in")) {
            if (args.getOptionProperties("in").size() != 1)
                throw new InternalError("Only one pairing allowed for the --in parameter");

            Properties p = args.getOptionProperties("in");

            aggregateArea = (AbstractZone) parseZoneReference(p, conn);
            aggregateAreaType = aggregateArea.getZoneLevel();
        }

        if (args.hasOption("top")) {
            top = args.getParsedOptionValue("top");
            if (top < 0)
                throw new InternalError("top parameter cannot have a value below 0");
        }

        // within
        int levelsDown = Integer.numberOfLeadingZeros(areaType.getSizeRank()) -
                Integer.numberOfLeadingZeros(aggregateAreaType.getSizeRank())
                - ((areaType == Zone.CAPITALS) ? 1 : 0);

        List<IZone> expansion = aggregateArea.getInnerZones(levelsDown, conn);

        if (areaType == Zone.CAPITALS)
            expansion = expansion.stream().filter(z -> ((City) z).isCapital()).collect(Collectors.toList());

        expansion.sort((s1, s2) -> {
            try {
                return Long.compare(s2.getTotalPopulation(conn), s1.getTotalPopulation(conn));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        List<IZone> res = ((top != -1) ? expansion.stream().limit(top).collect(Collectors.toList()) : expansion);

        showLeaderboard(res, conn);

        return res;
    }

    private static void showLeaderboard(List<IZone> zones, Connection conn) throws SQLException {
        for (IZone z : zones) {
            System.out.println(z.toString() + ": " + z.getTotalPopulation(conn));
        }
    }
}
