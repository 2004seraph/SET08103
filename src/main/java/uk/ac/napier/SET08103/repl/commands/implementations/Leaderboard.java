package uk.ac.napier.SET08103.repl.commands.implementations;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.napier.SET08103.model.concepts.City;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.model.concepts.World;
import uk.ac.napier.SET08103.model.concepts.zone.AbstractZone;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.model.concepts.zone.Zone;
import uk.ac.napier.SET08103.repl.commands.ICommand;
import uk.ac.napier.SET08103.reports.CapitalReport;
import uk.ac.napier.SET08103.reports.CityReport;
import uk.ac.napier.SET08103.reports.CountryReport;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import static uk.ac.napier.SET08103.repl.Repl.parseZoneReference;

/**
 * For generating every type of report, excluding population reports
 */
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

    @SuppressWarnings("CommentedOutCode")
    @Override
    public Object execute(final CommandLine args, final Connection conn)
            throws SQLException, RuntimeException, ParseException {
        assert args.getOptionValue("of") != null;

        // --of
        final Zone areaType = Zone.valueOf(args.getOptionValue("of").toUpperCase(Locale.ENGLISH));

        // --in
        Zone aggregateAreaType = Zone.WORLD;
        AbstractZone aggregateArea = World.INSTANCE;

        // --top
        int top = -1;

        // The implementation can actually do more than what the client asked for
//        if (areaType.getSizeRank() > Zone.COUNTRIES.getSizeRank())
//            throw new RuntimeException("Invalid combination of --of and --in parameters");


        if (args.hasOption("in")) {
            if (args.getOptionProperties("in").size() != 1)
                throw new RuntimeException("Only one pairing allowed for the --in parameter");

            final Properties p = args.getOptionProperties("in");

            aggregateArea = (AbstractZone) parseZoneReference(p, conn);
            aggregateAreaType = aggregateArea.getZoneLevel();

            if (areaType.getSizeRank() > aggregateAreaType.getSizeRank())
                throw new RuntimeException("Invalid combination of --of and --in parameters");
        }

        if (args.hasOption("top")) {
            top = args.getParsedOptionValue("top");
            if (top < 0)
                throw new RuntimeException("top parameter cannot have a value below 0");
        }

        // within
        final int levelsDown = Integer.numberOfLeadingZeros(areaType.getSizeRank()) -
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

        final List<IZone> res = ((top != -1) ? expansion.stream().limit(top).collect(Collectors.toList()) : expansion);

        showLeaderboard(areaType == Zone.CAPITALS, res, conn);

        return res;
    }

    private static void showLeaderboard(final boolean onlyCapitals, final List<IZone> zones, final Connection conn)
            throws SQLException {
        if (onlyCapitals) {
            final List<City> res = Zone.unwrapIZone(zones);
            CapitalReport.print((ArrayList<City>) res, conn);
            return;
        }

        switch (zones.get(0).getZoneLevel()) {
            case CAPITALS:
            case CITIES:
                {
                    final List<City> res = Zone.unwrapIZone(zones);
                    CityReport.print((ArrayList<City>) res, conn);
                }
                break;
            case COUNTRIES:
                {
                    final List<Country> res = Zone.unwrapIZone(zones);
                    CountryReport.print((ArrayList<Country>) res);
                }
                break;
            default:
                for (IZone z : zones) {
                    System.out.printf("%-15s %,15d%n", z.toString(), z.getTotalPopulation(conn));
                }
                break;
        }
    }
}
