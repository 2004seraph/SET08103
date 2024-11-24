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
import java.util.stream.Collectors;

public final class Region extends AbstractZone implements IFieldEnum<String>, IZone {

    public static Region fromName(String name, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + Country.table + " WHERE " + Country.regionField + " = ?")) {
            ps.setString(1, name);

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return new Region(name, Continent.fromDatabaseString(res.getString(Country.continentField)));
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
        final String cacheKey = this.getClass().getName() + "/" + continent + "/" + name + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return unwrapIZone(cacheMap.get(cacheKey));

        List<City> c = this.getInnerZones(conn)
                .stream()
                .flatMap(d -> {
                    try {
                        return d.getCities(conn).stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        cacheMap.put(cacheKey, wrapIZone(c));
        return c;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + continent + "/" + name + "/innerZones";
        if (cacheMap.containsKey(cacheKey))
            return cacheMap.get(cacheKey);

        // Will always be unique because country is a db entity
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT * FROM " + Country.table +
                        " WHERE " + Country.regionField + " = ?"
        );
        stmt.setString(1, name);

        List<IZone> countries = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                countries.add(
                        Country.fromCountryCode(
                                res.getString(Country.primaryKeyField), conn));
        }

        cacheMap.put(cacheKey, countries);

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
