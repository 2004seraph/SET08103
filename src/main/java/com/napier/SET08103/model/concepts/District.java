package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;

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
                "SELECT " + City.districtFieldName + ", " + City.countryCodeFieldName + " FROM " + City.tableName +
                        " WHERE LOWER( " + City.districtFieldName + " ) LIKE ? AND " + City.countryCodeFieldName + " = ?"
        );
        stmt.setString(1, name.toLowerCase());
        stmt.setString(2, country.countryCode);

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                District d = new District(res.getString(City.districtFieldName));
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
                "SELECT * FROM " + City.tableName +
                        " WHERE LOWER( " + City.districtFieldName + " ) LIKE ? ORDER BY " +
                        City.populationFieldName + " DESC"
        );
        stmt.setString(1, name.toLowerCase());

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                District d = new District(res.getString(City.districtFieldName));
                d.country = Country.fromCountryCode(res.getString(City.countryCodeFieldName), conn);
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
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + City.primaryKeyFieldName + " FROM " + City.tableName +
                        " WHERE " + City.districtFieldName + " = ?"
        );
        stmt.setString(1, name);

        List<City> cities = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                cities.add(City.fromId(res.getInt(City.primaryKeyFieldName), conn));
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
