package com.napier.SET08103.repl;

import com.napier.SET08103.model.concepts.*;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.repl.commands.Command;
import org.apache.commons.cli.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Basic read-evaluate-print-loop that parses argument data and hands it off to the relevant command
 */
public final class Repl {
    private Repl() { }

    /**
     * Prints CLI intro to the app for when the package is first invoked
     */
    public static void printWelcome() {
        System.out.println("This is a REPL prompt. Type commands.");
        System.out.println();
        System.out.println(
                "The results of all queries are cached in memory, " +
                        "and subsequent ones will make use of this cache where applicable.");
        printTopLevelHelpString();
    }

    /**
     * Prints help for the entire package
     */
    private static void printTopLevelHelpString() {
        System.out.println();
        System.out.println("Available commands: ");
        System.out.println();

        Arrays.asList(Command.asArray).forEach(e -> {
            System.out.println(e);
            printSubCommandHelpString(e);
            System.out.println();
        });
        System.out.println("Exit with CRTL+C, or by typing 'quit'");
    }

    /**
     * Prints parameter help for a given Command
     */
    private static void printSubCommandHelpString(Command command) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                command.toString().toLowerCase() + " [options]", command.Instance().getOptions());
    }

    /**
     * Command entry point to the REPL
     * @param args command arguments
     * @param conn database connection
     * @return an object representing the evaluated output of the command
     */
    public static Object parseAndRun(String[] args, Connection conn) throws RuntimeException {
        if (args.length == 0) {
            printTopLevelHelpString();
            throw new java.lang.Error("No args supplied");
        }

        Command command;
        try {
            command = Command.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown subcommand");
            printTopLevelHelpString();
            throw new RuntimeException(e);
        }

        try {
            CommandLine subArgs = new DefaultParser().parse(
                    command.Instance().getOptions(),
                    args,
                    false // true = throw, false so it ignores the first arg
            );
            return command.Instance().execute(subArgs, conn);

        } catch (ParseException e) {
            System.out.println("Syntax Error: ");
            printSubCommandHelpString(command);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.out.println("Database Error: ");
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            System.out.println("Command Error: ");
            printSubCommandHelpString(command);
            throw new RuntimeException(e);
        }
    }

    public static Object parseAndRun(Connection conn, String... args) throws RuntimeException {
        return parseAndRun(args, conn);
    }

    /**
     * Parses zone references to IZone instances:
     *      "continent:Europe"
     *      "district:texas"
     *      "world"
     *      etc.
     * Either a name or a primary key may be supplied where relevant.
     * <p>
     * The user may use any case combination they want.
     *
     * @param p Parsed key:value properties for a specific command line argument
     * @param conn Connects to the database to get the specific zone instance referenced
     * @return An IZone instance which you can call getZoneType() on to know what to cast it to
     * @throws RuntimeException Any kind of parse error
     * @throws SQLException Not found in the database, or any other issue relating to the database
     */
    public static IZone parseZoneReference(Properties p, Connection conn)
            throws RuntimeException, SQLException {

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
                } catch (IllegalArgumentException e) {
                    return Country.fromCountryCode(p.get(zoneType).toString(), conn);
                }
            case "district":
                return District.fromName(p.get(zoneType).toString(), conn);
            case "city":
                try {
                    return City.fromName(p.get(zoneType).toString(), conn);
                } catch (IllegalArgumentException e) {
                    return City.fromId(Integer.parseInt(p.get(zoneType).toString()), conn);
                }
            default:
                throw new RuntimeException("Could not parse zone reference");
        }
    }
}
