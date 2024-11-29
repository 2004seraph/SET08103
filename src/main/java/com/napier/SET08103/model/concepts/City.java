package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.model.db.IEntity;
import com.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a City (and a Capital) entity from the database
 */
public final class City extends AbstractZone implements IEntity {

    // no spelling mistakes
    public static final String TABLE = "city";
    public static final String PRIMARY_KEY = TABLE + ".ID";
    public static final String POPULATION = TABLE + ".Population";
    public static final String NAME = TABLE + ".Name";
    public static final String DISTRICT = TABLE + ".District";
    public static final String COUNTRY_CODE = TABLE + ".CountryCode";

//    SELECT *
//    FROM city
//    RIGHT OUTER JOIN country
//    ON city.ID = country.Capital
//    WHERE ID = 1
    private static final String CREATION_SQL =
            Model.buildStatement(
                    "SELECT *",
                    "FROM", City.TABLE,
                    "LEFT OUTER JOIN", Country.TABLE,
                    "ON", City.PRIMARY_KEY, "=", Country.CAPITAL,
                    "WHERE", City.PRIMARY_KEY, "= ?");

    /**
     * Returns a City instance with a given primary key
     * @param id
     * @param conn
     * @return
     * @throws SQLException No entries found with that id
     */
    public static City fromId(int id, Connection conn) throws SQLException {
        // Note to self: if this city is NOT a capital, ALL Country fields will be NULL
        try (PreparedStatement ps = conn.prepareStatement(CREATION_SQL)) {
            ps.setInt(1, id);

            try (ResultSet res = ps.executeQuery()) {
                if (!res.next())
                    throw new IllegalArgumentException("No city with ID: " + id);

                City c = new City(id, res.getInt(Country.CAPITAL));

                c.population = res.getInt(POPULATION);
                c.name = res.getString(NAME);

                // Some cities can be in a NULL district
                String districtName = res.getString(City.DISTRICT);
                c.parentZone = (!Objects.equals(districtName, District.nullFieldValue)) ?
                        District.fromName(
                                districtName,
                                Country.fromCountryCode(res.getString(COUNTRY_CODE), conn),
                                conn)
                        : Country.fromCountryCode(res.getString(City.COUNTRY_CODE), conn);

                return c;
            }
        }
    }

    /**
     * In the event of a name clash, it will return the city with the higher population.
     * @param name The name of the city
     * @param conn The db connection
     * @return An instance of City
     * @throws SQLException Use within your try/catch statements
     */
    public static City fromName(String name, Connection conn) throws SQLException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Invalid City name");

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM " + TABLE +
                        " WHERE LOWER( Name ) LIKE ? ORDER BY " + POPULATION + " DESC"
        );
        stmt.setString(1, "%" + name.toLowerCase() + "%");

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return fromId(res.getInt(PRIMARY_KEY), conn);
            } else
                throw new IllegalArgumentException("No city with name: " + name);
        }
    }

    /**
     * Returns every capital in the world
     * @param conn
     * @return
     * @throws SQLException
     */
    public static List<City> allCapitals(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + Country.CAPITAL + " FROM " + Country.TABLE
        );

        List<City> capitals = new ArrayList<>();

        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
                int key = res.getInt(1);
                if (key != 0)
                    capitals.add(City.fromId(key, conn));
            }
        }

        return capitals;
    }

    /**
     * Code duplication, but I cannot work out how to invert the program-data dependency here.
     *
     * This function is only meant to be called by the Country class to set its own capital City instance,
     * using the public function causes infinite recursion.
     */
    static City fromIdAsCapital(int id, Country country, Connection conn) throws SQLException {

        // Note to self: if this city is NOT a capital, ALL Country fields will be NULL
        try (PreparedStatement ps = conn.prepareStatement(CREATION_SQL)) {
            ps.setInt(1, id);

            try (ResultSet res = ps.executeQuery()) {
                if (!res.next())
                    throw new IllegalArgumentException("No city with ID: " + id);

                City c = new City(id, res.getInt(Country.CAPITAL));

                c.population = res.getInt(POPULATION);
                c.name = res.getString(NAME);

                // Some cities can be in a NULL district
                // Country is pre-computed this time
                String districtName = res.getString(City.DISTRICT);
                c.parentZone = (!Objects.equals(districtName, District.nullFieldValue)) ?
                        District.fromName(
                                districtName,
                                country,
                                conn)
                        : country;

                return c;
            }
        }
    }

    // Primary key column: "ID"
    public final int id;
    private final Zone zone;

    private IZone parentZone;

    private int population;
    private String name;

    private City(int primaryKey, int countryCapital) {
        this.id = primaryKey;
        this.zone = (id == countryCapital) ? Zone.CAPITALS : Zone.CITIES;
    }

    public District getDistrict() {
        IZone parent = getOuterZone();
        switch (parent.getZoneLevel()) {
            case COUNTRIES:
                return null;
            case DISTRICTS:
                return (District) parent;
            default:
                throw new RuntimeException("Cannot find parent");
        }
    }

    public Country getCountry() {
        IZone parent = getOuterZone();
        switch (parent.getZoneLevel()) {
            case COUNTRIES:
                return (Country) parent;
            case DISTRICTS:
                return (Country) parent.getOuterZone();
            default:
                throw new RuntimeException("Cannot find parent");
        }
    }

    public boolean isCapital() {
        return this.zone == Zone.CAPITALS;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        return List.of(this);
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        // required behaviour for tree searching, because sometimes a city can be in a NULL district
        return List.of(this);
    }

    @Override
    public String getPrimaryKey() {
        return String.valueOf(this.id);
    }

    @Override
    public Zone getZoneLevel() {
        return zone;
    }

    @Override
    public IZone getOuterZone() {
        return parentZone;
    }

    // INFINITE RECURSION IF THIS IS NOT OVERRIDDEN BY THIS CLASS !!!!
    @Override
    public long getTotalPopulation(Connection conn) throws SQLException {
        return this.population;
    }

    @Override
    public String toString() {
        return name;
    }
}
