package com.napier.SET08103.repl;

import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.concepts.*;
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

    public static final class PopulationOf {
        private PopulationOf() {
        }

        public static final String name = "total";

        public static final Options options = new Options()
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

        public static void execute(CommandLine args, Connection conn) throws SQLException, InternalError, ParseException {
            IZone target = parseZoneReference(args.getOptionProperties("in"), conn);
            System.out.println(target.toString() + ": " + target.getTotalPopulation(conn));
        }
    }


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

                aggregateArea = (AbstractZone) parseZoneReference(p, conn);
                aggregateAreaType = aggregateArea.getZoneLevel();
            }

            if (args.hasOption("top")) {
                top = args.getParsedOptionValue("top");
                if (top < 0)
                    throw new InternalError();
            }

            // within
            int levelsDown = Integer.numberOfLeadingZeros(areaType.getSizeRank()) -
                    Integer.numberOfLeadingZeros(aggregateAreaType.getSizeRank())
                    - ((areaType == Zone.CAPITALS) ? 1 : 0);

            List<IZone> expansion = aggregateArea.getInnerZones(levelsDown, conn);

            if (areaType == Zone.CAPITALS)
                expansion = expansion.stream().filter(z -> ((City)z).isCapital()).collect(Collectors.toList());

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

    private static IZone parseZoneReference(Properties p, Connection conn) throws InternalError, SQLException {
        switch (p.keys().nextElement().toString().replace('_', ' ')) {
            case "world":
                return World.instance;
            case "continent":
                return Continent.likeDatabaseString(p.get("continent").toString(), conn);
            case "region":
                return Region.fromName(p.get("region").toString(), conn);
            case "country":
                try {
                    return Country.fromName(p.get("country").toString(), conn);
                } catch (Exception e) {
                    return Country.fromCountryCode(p.get("country").toString(), conn);
                }
            case "district":
                return District.fromName(p.get("district").toString(), conn);
            case "city":
                try {
                    return City.fromName(p.get("city").toString(), conn);
                } catch (Exception e) {
                    return City.fromId(Integer.parseInt(p.get("city").toString()), conn);
                }
            default:
                throw new InternalError();
        }
    }

}
