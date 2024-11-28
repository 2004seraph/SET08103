package com.napier.SET08103.repl;

import com.napier.SET08103.model.concepts.*;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.Command;
import org.apache.commons.cli.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Basic read-evaluate-print-loop that parses argument data and hands it off to the relevant command
 */
public final class Repl {
    private Repl() { }

    private static final String COMMAND = "pop";

    public static void main(String[] args) {
        // Left here as an example, as a database connection is inaccessible here

//        try (final App app = new App()) {
//            app.connect(
//                    Objects.requireNonNullElse(
//                            System.getenv("MYSQL_HOST"),
//                            "localhost"),
//                    Objects.requireNonNullElse(
//                            System.getenv("MYSQL_ROOT_PASSWORD"),
//                            "default")
//            );
//            parseAndRun(new String[] { "leaderboard", "--top", "20", "--of", "capitals" , "--in", "continent:Europe" }, app.getConnectionForIntegrationTesting());
//            System.out.println();
//            parseAndRun(new String[] { "leaderboard", "--top", "20", "--of", "cities" , "--in", "continent:Europe" }, app.getConnectionForIntegrationTesting());
//            parseAndRun(new String[] { "total", "--in", "city:london" }, app.getConnectionForIntegrationTesting());
//        }
    }

    public static void printHelpString(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(COMMAND + " subcommand [options]", options);
    }

    /**
     * Command entry point to the REPL, handles all possible exceptions in the application gracefully
     * @param args
     * @param conn
     */
    public static void parseAndRun(String[] args, Connection conn) {
        if (args.length == 0) {
            System.out.println("Usage: " + COMMAND + " <total/leaderboard> [options]");
            return;
        }

        Command command;
        try {
            command = Command.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown subcommand");
            return;
        }

        try {
            CommandLine subArgs = new DefaultParser().parse(
                    command.Instance().getOptions(),
                    args,
                    false // true = throw, false so it ignores the first arg
            );
            command.Instance().execute(subArgs, conn);
        } catch (ParseException e) {
            System.out.println("Syntax Error: ");
            printHelpString(command.Instance().getOptions());
        } catch (SQLException e) {
            System.out.println("Database Error: ");
            System.out.println(e.getMessage());
        } catch (InternalError | NullPointerException | IllegalArgumentException e) {
            System.out.println("Command Error: ");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Parses zone references to IZone instances:
     *      "continent:Europe"
     *      "district:texas"
     *      "world"
     *      etc.
     *
     * The user may use any case combination they want.
     *
     * @param p Parsed key:value properties for a specific command line argument
     * @param conn Connects to the database to get the specific zone instance referenced
     * @return
     * @throws InternalError
     * @throws SQLException
     */
    public static IZone parseZoneReference(Properties p, Connection conn)
            throws InternalError, SQLException, IllegalArgumentException {

        final String zoneType = p.keys().nextElement().toString().replace('_', ' ');

        switch (zoneType.toLowerCase()) {
            case "world":
                return World.INSTANCE;
            case "continent":
                return Continent.likeDatabaseString(p.get(zoneType).toString(), conn);
            case "region":
                return Region.fromName(p.get(zoneType).toString(), conn);
            case "country":
                try {
                    return Country.fromName(p.get(zoneType).toString(), conn);
                } catch (Exception e) {
                    return Country.fromCountryCode(p.get(zoneType).toString(), conn);
                }
            case "district":
                return District.fromName(p.get(zoneType).toString(), conn);
            case "city":
                try {
                    return City.fromName(p.get(zoneType).toString(), conn);
                } catch (Exception e) {
                    return City.fromId(Integer.parseInt(p.get(zoneType).toString()), conn);
                }
            default:
                throw new InternalError();
        }
    }
}
