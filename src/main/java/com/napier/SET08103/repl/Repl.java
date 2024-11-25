package com.napier.SET08103.repl;

import com.napier.SET08103.App;
import org.apache.commons.cli.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;


/**
 * pop leaderboard
 *
 *
 */


public final class Repl {
    private Repl() { }

    private static final String COMMAND = "pop";

    public static void main(String[] args) throws SQLException, ParseException {
        try (final App app = new App()) {
            app.connect(
                    Objects.requireNonNullElse(
                            System.getenv("MYSQL_HOST"),
                            "localhost"),
                    Objects.requireNonNullElse(
                            System.getenv("MYSQL_ROOT_PASSWORD"),
                            "default")
            );

//        parse(new String[] { "-Dkey=1", "-Dbumbo=1" }, null);options.addOption(Option.builder("D").hasArgs().valueSeparator('=').build());;commandLine.getOptionProperties("D");//{bumbo=1, key=1}
//        parseAndRun(new String[] { "--test build" }, null);
//            parseAndRun(new String[] { "leaderboard", "--ofqwq", "countries" }, app.getConnectionForIntegrationTesting());
//            System.out.println();
            parseAndRun(new String[] { "leaderboard", "--top", "20", "--of", "capitals" , "--in", "continent:Europe" }, app.getConnectionForIntegrationTesting());
            System.out.println();
            parseAndRun(new String[] { "leaderboard", "--top", "20", "--of", "cities" , "--in", "continent:Europe" }, app.getConnectionForIntegrationTesting());

//            System.out.println();
//            parseAndRun(new String[] { "leaderboard", "--top", "10", "--of", "capitals" , "--in", "continent:Europe" }, app.getConnectionForIntegrationTesting());

        }
//        options.addOption("b", true, "some message");
    }

    public static void printHelpString(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(COMMAND + " subcommand [options]", options);
    }

    public static void parseAndRun(String[] args, Connection conn) throws SQLException {
        try {
            switch (args[0]) {
                case Commands.Leaderboard.name:
//                    System.out.println(Commands.Leaderboard.name);
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
                default:
                    System.out.println("nothing");
                    break;
            }

        }  catch (InternalError e) {
            System.out.println("Command Error: ");
            System.out.println(e.getMessage());
            printHelpString(Commands.Leaderboard.options);
        }
    }
}
