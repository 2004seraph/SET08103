package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class City extends AbstractZone implements IEntity {

    // no spelling mistakes
    public static final String table = "city";
    public static final String primaryKeyField = table + ".ID";
    public static final String populationField = table + ".Population";
    public static final String nameField = table + ".Name";
    public static final String districtField = table + ".District";
    public static final String countryCodeField = table + ".CountryCode";

    public static City fromId(int id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + table + " WHERE " + primaryKeyField + " = ?")) {
            ps.setInt(1, id);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    City c = new City(
                            id,
                            Country.fromCountryCode(res.getString(countryCodeField), conn),
                            conn);
                    c.population = res.getInt(populationField);
                    c.name = res.getString(nameField);

                    return c;
                }
                else
                    throw new IllegalArgumentException("No city with ID: " + id);
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
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM " + table +
                        " WHERE LOWER( Name ) LIKE ? ORDER BY " + populationField +" DESC"
        );
        stmt.setString(1, name.toLowerCase());

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                City c = new City(
                        res.getInt(primaryKeyField),
                        Country.fromCountryCode(res.getString(countryCodeField), conn),
                        conn);
                c.population = res.getInt(populationField);
                c.name = res.getString(nameField);

                return c;
            } else
                throw new IllegalArgumentException("No city with name: " + name);
        }
    }

    public static List<City> capitals(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + Country.capitalField + " FROM " + Country.table
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

    private static District setInstanceDistrict(int id, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + City.districtField + ", " + countryCodeField + " " +
                        " FROM " + table +
                        " WHERE " + primaryKeyField +
                        " = ?"
        );
        stmt.setInt(1, id);

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return District.fromName(
                        res.getString(City.districtField),
                        Country.fromCountryCode(res.getString(countryCodeField), conn),
                        conn);
            } else
                throw new IllegalArgumentException("No city with ID: " + id);
        }
    }

    private static Zone setInstanceZone(int id, Connection conn) throws SQLException {
        PreparedStatement checkCapital = conn.prepareStatement(
                "SELECT * FROM " + Country.table + " WHERE " + Country.capitalField + " = ?"
        );
        checkCapital.setInt(1, id);

        try (checkCapital; ResultSet answer = checkCapital.executeQuery()) {
            if (answer.next())
                return Zone.CAPITALS;
        }
        return Zone.CITIES;
    }

    // Primary key column: "ID"
    public final int id;
    private final Zone zone;

    private IZone parentZone;
    private int population;
    private String name;

    private City(int primaryKey, Country country, Connection conn) throws SQLException {
        this.id = primaryKey;
        this.zone = setInstanceZone(this.id, conn);

        this.parentZone = setInstanceDistrict(this.id, conn);
        if (this.parentZone == null)
            this.parentZone = country;
    }

    public boolean isCapital() {
        return this.zone == Zone.CAPITALS;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        return List.of();
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
