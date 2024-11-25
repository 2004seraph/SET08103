package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.db.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class World extends AbstractZone {
    public static final World instance = new World();

    private World() { }

    private static void preload(Connection conn) throws SQLException {
        instance.getInnerZones(conn);
    }

    @Override
    public Zone getZoneLevel() {
        return Zone.WORLD;
    }

    @Override
    public IZone getOuterZone() {
        return null;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        return Continent.getAll().stream().map(c -> (IZone)c).collect(Collectors.toList());
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/instance" + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return unwrapIZone(cacheMap.get(cacheKey));

        List<City> c = getInnerZones(conn)
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
    public long getTotalPopulation(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT",
                        "SUM(", Country.POPULATION, ") AS Total",
                        "FROM", Country.TABLE
                )
        )) {
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return res.getLong("Total");
                }
                else
                    throw new InternalError("Database error");
            }
        }
    }
}
