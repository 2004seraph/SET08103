package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Region implements IFieldEnum<String>, IZone {

    public static Region fromName(String name, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + Country.tableName + " WHERE " + Country.regionFieldName + " = ?")) {
            ps.setString(1, name);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return new Region(name, Continent.fromName(res.getString(Country.continentFieldName)));
                }
                else
                    throw new IllegalArgumentException("No region with name: " + name);
            }
        }
    }

    private final String name;
    private final Continent continent;

    private Region(String name, Continent continent) {
        this.name = name;
        this.continent = continent;
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        return this.getInnerZones(conn)
                .stream()
                .flatMap(d -> {
                    try {
                        return d.getCities(conn).stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM " + Country.tableName +
                        " WHERE " + Country.regionFieldName + " = ?"
        );
        stmt.setString(1, name);

        List<IZone> countries = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                countries.add(
                        Country.fromCountryCode(
                                res.getString(Country.primaryKeyFieldName), conn));
        }

        return countries;
    }

    @Override
    public String getValue() {
        return this.name;
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
        return Zone.REGIONS;
    }

    @Override
    public IZone getOuterZone() {
        return this.continent;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
