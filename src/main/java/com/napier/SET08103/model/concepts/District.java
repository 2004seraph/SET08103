package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;
import com.napier.SET08103.model.db.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class District extends AbstractZone implements IFieldEnum<String>, IZone {

    public static final String nullFieldValue = "–"; // Don't be fooled, this is a weird Unicode character

    public static District fromName(String name, String countryCode, Connection conn) throws SQLException {
        return District.fromName(name, Country.fromCountryCode(countryCode, conn), conn);
    }

    public static District fromName(String name, Country country, Connection conn) throws SQLException {
        if (Objects.equals(name, nullFieldValue))
            return null;

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + City.districtField + ", " + City.countryCodeField + " FROM " + City.table +
                        " WHERE LOWER( " + City.districtField + " ) LIKE ? AND " + City.countryCodeField + " = ?"
        );
        stmt.setString(1, name.toLowerCase());
        stmt.setString(2, country.countryCode);

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                District d = new District(res.getString(City.districtField));
                d.country = country;
                return d;
            } else
                throw new IllegalArgumentException("No district with name: " + name + ", and country: " + country);
        }
    }

    public static District fromName(String name, Connection conn) throws SQLException {
        if (Objects.equals(name, nullFieldValue))
            return null;

        PreparedStatement stmt = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT",

                        City.countryCodeField, ",",
                        City.districtField, ",",
                        "SUM(", City.populationField, ")", "as Total",

                        "FROM", City.table,
                        "WHERE", "LOWER(", City.districtField, ")", "LIKE ?",
                        "GROUP BY", City.countryCodeField, ",", City.districtField,
                        "ORDER BY Total", "DESC"
                )
        );
        stmt.setString(1, "%" + name.toLowerCase() + "%");

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                District d = new District(res.getString(City.districtField));
                d.country = Country.fromCountryCode(res.getString(City.countryCodeField), conn);
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
        // Will always be unique values, due to primary key usage
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + City.primaryKeyField + " FROM " + City.table +
                        " WHERE " + City.districtField + " = ?" + " AND " +
                        City.countryCodeField + " = ?"
        );
        stmt.setString(1, name);
        stmt.setString(2, country.countryCode);

        List<City> cities = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                cities.add(City.fromId(res.getInt(City.primaryKeyField), conn));
        }

        return cities;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        return getCities(conn).stream().map(c -> (IZone)c).collect(Collectors.toList());
    }

    @Override
    public String getValue() {
        return name;
    }

    @Override
    public PopulationInfo getPopulation() {
        return new PopulationInfo(
                this,
                0,
                0
        );
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
    public String toString() {
        return name;
    }
}
