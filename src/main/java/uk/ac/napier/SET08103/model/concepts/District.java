package uk.ac.napier.SET08103.model.concepts;

import uk.ac.napier.SET08103.model.Model;
import uk.ac.napier.SET08103.model.concepts.zone.AbstractZone;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.model.concepts.zone.Zone;
import uk.ac.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class District extends AbstractZone implements IFieldEnum<String> {
    // Don't be fooled, this is a weird Unicode character in the database
    public static final String nullFieldValue = "–";

    /**
     * For selecting a district exactly, by specifying a parent country primary key
     * @param countryCode An explicit country code this district is contained within
     * @throws SQLException If the given country does not exist, or if it has no such district with the
     * given name
     */
    public static District fromName(String name, String countryCode, Connection conn) throws SQLException {
        return District.fromName(name, Country.fromCountryCode(countryCode, conn), conn);
    }

    /**
     * For selecting a district exactly, by specifying a parent country instance
     * @param country An explicit country instance this district is contained within
     * @throws SQLException If the given country does not exist, or if it has no such district with the
     * given name
     */
    public static District fromName(String name, Country country, Connection conn) throws SQLException {
        if (Objects.equals(name, nullFieldValue))
            return null;

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + City.DISTRICT + ", " + City.COUNTRY_CODE + " FROM " + City.TABLE +
                        " WHERE LOWER( " + City.DISTRICT + " ) LIKE ? AND " + City.COUNTRY_CODE + " = ?"
        );
        stmt.setString(1, name.toLowerCase(Locale.ENGLISH));
        stmt.setString(2, country.countryCode);

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                District d = new District(res.getString(City.DISTRICT));
                d.country = country;
                return d;
            } else
                throw new IllegalArgumentException("No district with name: " + name + ", and country: " + country);
        }
    }

    /**
     * Returns a district from a name, in the event of multiple matches, the district with the higher
     * population is used
     * @throws SQLException No results found for this name
     */
    public static District fromName(String name, Connection conn) throws SQLException {
        if (Objects.equals(name, nullFieldValue))
            return null;

        PreparedStatement stmt = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT",

                        City.COUNTRY_CODE, ",",
                        City.DISTRICT, ",",
                        "SUM(", City.POPULATION, ")", "as Total",

                        "FROM", City.TABLE,
                        "WHERE", "LOWER(", City.DISTRICT, ")", "LIKE ?",
                        "GROUP BY", City.COUNTRY_CODE, ",", City.DISTRICT,
                        "ORDER BY Total", "DESC"
                )
        );
        stmt.setString(1, "%" + name.toLowerCase(Locale.ENGLISH) + "%");

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                District d = new District(res.getString(City.DISTRICT));
                d.country = Country.fromCountryCode(res.getString(City.COUNTRY_CODE), conn);
                return d;
            } else
                throw new IllegalArgumentException("No district with name: " + name);
        }
    }

    private final String name;
    private Country country;

    private District(String name) {
        this.name = name;
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + country.countryCode + "/" + name + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return Zone.unwrapIZone(cacheMap.get(cacheKey));

        // Will always be unique values, due to primary key usage
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + City.PRIMARY_KEY + " FROM " + City.TABLE +
                        " WHERE " + City.DISTRICT + " = ?" + " AND " +
                        City.COUNTRY_CODE + " = ?"
        );
        stmt.setString(1, name);
        stmt.setString(2, country.countryCode);

        List<City> cities = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                cities.add(City.fromId(res.getInt(City.PRIMARY_KEY), conn));
        }

        cacheMap.put(cacheKey, Zone.wrapIZone(cities));

        return cities;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        return Zone.wrapIZone(getCities(conn));
    }

    @Override
    public String getValue() {
        return name;
    }

    @Override
    public Zone getZoneLevel() {
        return Zone.DISTRICTS;
    }

    @Override
    public IZone getOuterZone() {
        return this.country;
    }

    @Override
    public long getTotalPopulation(Connection conn) throws SQLException {
        return getCities(conn).stream().mapToLong(c -> c.getTotalPopulation(conn)).reduce(0, Long::sum);
    }

    @Override
    public String toString() {
        return name;
    }
}
