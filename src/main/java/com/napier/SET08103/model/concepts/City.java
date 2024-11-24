package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class City extends AbstractZone implements IEntity, IZone {

    public static final String tableName = "city";
    public static final String primaryKeyFieldName = "ID";
    public static final String populationFieldName = "Population";
    public static final String nameFieldName = "Name";
    public static final String districtFieldName = "District";
    public static final String countryCodeFieldName = "CountryCode";

    public static City fromId(int id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + tableName + " where " + primaryKeyFieldName + " = ?")) {
            ps.setInt(1, id);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    City c = new City(
                            id,
                            Country.fromCountryCode(res.getString(countryCodeFieldName), conn),
                            conn);
                    c.population = res.getInt(populationFieldName);
                    c.name = res.getString(nameFieldName);

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
                "SELECT * FROM " + tableName +
                        " WHERE LOWER( Name ) LIKE ? ORDER BY " + populationFieldName +" DESC"
        );
        stmt.setString(1, name.toLowerCase());

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                City c = new City(
                        res.getInt(primaryKeyFieldName),
                        Country.fromCountryCode(res.getString(countryCodeFieldName), conn),
                        conn);
                c.population = res.getInt(populationFieldName);
                c.name = res.getString(nameFieldName);

                return c;
            } else
                throw new IllegalArgumentException("No city with name: " + name);
        }
    }

    public static List<City> capitals(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + Country.capitalFieldName + " FROM " + Country.tableName
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
                "SELECT " + City.districtFieldName + ", " + countryCodeFieldName + " " +
                        " FROM " + tableName +
                        " WHERE " + primaryKeyFieldName +
                        " = ?"
        );
        stmt.setInt(1, id);

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return District.fromName(
                        res.getString(City.districtFieldName),
                        Country.fromCountryCode(res.getString(countryCodeFieldName), conn),
                        conn);
            } else
                throw new IllegalArgumentException("No city with ID: " + id);
        }
    }

    private static Zone setInstanceZone(int id, Connection conn) throws SQLException {
        PreparedStatement checkCapital = conn.prepareStatement(
                "SELECT * FROM " + Country.tableName + " WHERE " + Country.capitalFieldName + " = ?"
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
        return List.of();
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

    @Override
    public PopulationInfo getPopulation() {
        return new PopulationInfo(
                this,
                this.population,
                this.population
        );
    }

    @Override
    public String toString() {
        return name;
    }
}
