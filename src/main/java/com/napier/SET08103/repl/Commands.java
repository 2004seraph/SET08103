package com.napier.SET08103.repl;

import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.concepts.Continent;
import com.napier.SET08103.model.concepts.Region;
import com.napier.SET08103.model.concepts.World;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public final class Commands {
    private Commands() { }

    public static final class Leaderboard {
        private Leaderboard() { }

        public static final String name = "leaderboard";

        public static final Options options = new Options()
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

        public static void execute(CommandLine args, Connection conn) throws SQLException, InternalError, ParseException {
            assert args.getOptionValue("of") != null;

            // --of
            Zone areaType;

            // --in
            Zone aggregateAreaType = Zone.WORLD;
            AbstractZone aggregateArea = World.instance;

            // --top
            int top = -1;

            try {
                areaType = Zone.valueOf(args.getOptionValue("of").toUpperCase());
                if (areaType.getSizeRank() > Zone.COUNTRIES.getSizeRank())
                    throw new InternalError();
            } catch (IllegalArgumentException e) {
                throw new InternalError(e);
            }

            if (args.hasOption("in")) {
                if (args.getOptionProperties("in").size() != 1)
                    throw new InternalError();

                Properties p = args.getOptionProperties("in");

                switch (p.keys().nextElement().toString()) {
                    case "world":
                        break;
                    case "continent":
                        aggregateAreaType = Zone.CONTINENTS;
                        aggregateArea = Continent.likeDatabaseString(p.get("continent").toString(), conn);
                        break;
                    case "region":
                        aggregateAreaType = Zone.REGIONS;
                        aggregateArea = Region.fromName(p.get("region").toString(), conn);
                        break;
                    default:
                        throw new InternalError();
                }
            }

            if (args.hasOption("top")) {
//                System.out.println(args.getOptionValue("top") + " ");
                top = args.getParsedOptionValue("top");
                if (top < 0)
                    throw new InternalError();
            }

            // within
            // world should be passed differently
            int levelsDown = Integer.numberOfLeadingZeros(areaType.getSizeRank()) -
                    Integer.numberOfLeadingZeros(aggregateAreaType.getSizeRank());

            List<IZone> expansion = aggregateArea.getInnerZones(levelsDown, conn);

            expansion.sort((s1, s2) -> {
                try {
                    return Long.compare(s2.getTotalPopulation(conn), s1.getTotalPopulation(conn));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            showLeaderboard(
                    ((top != -1) ? expansion.stream().limit(top).collect(Collectors.toList()) : expansion),
                    conn);
        }

        private static void showLeaderboard(List<IZone> zones, Connection conn) throws SQLException {
            for (IZone z : zones) {
                System.out.println(z.toString() + ": " + z.getTotalPopulation(conn));
            }
        }
    }

}
