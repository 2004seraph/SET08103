package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class City implements IEntity, IZone {

    public static final String tableName = "city";
    public static final String primaryKeyFieldName = "ID";
    public static final String populationFieldName = "Population";
    public static final String nameFieldName = "Name";

    public static City fromId(int id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + tableName + " where " + primaryKeyFieldName + " = ?")) {
            ps.setInt(1, id);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    City c = new City(id, conn);
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
                "SELECT * FROM " + tableName + " WHERE LOWER( Name ) LIKE ? ORDER BY " + populationFieldName +" DESC"
        );
        stmt.setString(1, name.toLowerCase());

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                City c = new City(res.getInt(primaryKeyFieldName), conn);
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

    private static Zone setZone(int id, Connection conn) throws SQLException {
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

    private int population;
    private String name;

    private City(int primaryKey, Connection conn) throws SQLException {
        this.id = primaryKey;
        this.zone = setZone(this.id, conn);
    }

    public boolean isCapital() {
        return this.zone == Zone.CAPITALS;
    }

    @Override
    public String getPrimaryKey() {
        return String.valueOf(this.id);
    }

    @Override
    public Zone GetZoneLevel() {
        return zone;
    }

    @Override
    public int getPopulation() {
        return this.population;
    }

    @Override
    public String toString() {
        return name;
    }
}
