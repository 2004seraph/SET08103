package com.napier.SET08103.repl;

import org.apache.commons.cli.*;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * pop leaderboard
 *
 *
 */


public final class Repl {
    private Repl() { }

    private static final String COMMAND = "pop";

    public static void main(String[] args) {
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

    public static void parseAndRun(String[] args, Connection conn) {
        try {
            switch (args[0]) {
                case Commands.Leaderboard.name:
                    try {
                        CommandLine subArgs = new DefaultParser().parse(
                                Commands.Leaderboard.options,
                                args,
                                false // true = throw, false so it ignores the first arg
                        );

                        Commands.Leaderboard.execute(subArgs, conn);
                    } catch (ParseException e) {
                        printHelpString(Commands.Leaderboard.options);
                    }
                    break;
                case Commands.PopulationOf.name:
                    try {
                        CommandLine subArgs = new DefaultParser().parse(
                                Commands.PopulationOf.options,
                                args,
                                false
                        );

                        Commands.PopulationOf.execute(subArgs, conn);
                    } catch (ParseException e) {
                        printHelpString(Commands.PopulationOf.options);
                    }
                    break;
                default:
                    System.out.println("Usage: " + COMMAND + " <total/leaderboard> [options]");
                    break;
            }
        } catch (SQLException e) {
            System.out.println("Database Error: ");
            System.out.println(e.getMessage());

        } catch (InternalError | NullPointerException | IllegalArgumentException e) {
            System.out.println("Command Error: ");
            System.out.println(e.getMessage());
        }
    }
}
