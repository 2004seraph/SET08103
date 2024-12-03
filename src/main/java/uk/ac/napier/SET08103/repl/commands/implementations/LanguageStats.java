package uk.ac.napier.SET08103.repl.commands.implementations;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.napier.SET08103.model.Model;
import uk.ac.napier.SET08103.model.concepts.Country;
import uk.ac.napier.SET08103.repl.commands.ICommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class LanguageStats implements ICommand {
    @Override
    public Options getOptions() {
        return new Options();
    }

    //SELECT language_populations.`Language`, SUM(language_populations.Speakers) AS Total FROM
    //(
    //    SELECT
    //        country.Code,
    //        countrylanguage.`Language`,
    //        country.Population,
    //        ((countrylanguage.Percentage / 100) * country.Population) AS Speakers
    //
    //    FROM country
    //    INNER JOIN countrylanguage
    //    ON country.Code = countrylanguage.CountryCode
    //) AS language_populations
    //GROUP BY language_populations.`Language`
    //ORDER BY Total
    //DESC
    //LIMIT 5;
    @Override
    public Object execute(CommandLine args, Connection conn) throws SQLException, RuntimeException, ParseException {
        try (PreparedStatement stmt = conn.prepareStatement(Model.buildStatement(
                "SELECT language_populations.`Language` AS Lang, SUM(language_populations.Speakers) AS Total",
                "FROM",

                // Populations of each language in each country
                "(",
                    "SELECT",
                        Country.PRIMARY_KEY,            ",",
                        "countrylanguage.`Language`",   ",",
                        Country.POPULATION,             ",",
                        "((countrylanguage.Percentage / 100) *", Country.POPULATION, ") AS Speakers",
                    "FROM", Country.TABLE,
                    "INNER JOIN", "countrylanguage",
                    "ON", Country.PRIMARY_KEY, "=", "countrylanguage.CountryCode",
                ") AS language_populations",

                "GROUP BY Lang",
                "ORDER BY Total",
                "DESC",
                "LIMIT 5"
        ))) {

            try (ResultSet res = stmt.executeQuery()) {
                // Print header
                System.out.printf(
                        "%-16s %-16s%n",
                        "Language", "Speakers");

                while (res.next()) {
                    System.out.printf(
                            "%-16s %-16d%n",
                            res.getString("Lang"), res.getLong("Total"));
                }
            }
        }

        return null;
    }
}
