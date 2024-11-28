package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Root of the tree of zones
 */
public class World extends AbstractZone {
    public static final World INSTANCE = new World();

    private World() { }

    /**
     * Will load the entire tree structure of zones into memory, will mean no delays in any of the
     * query methods
     * @param conn
     * @throws SQLException
     */
    public static void preload(Connection conn) throws SQLException {
        INSTANCE.getInnerZones(conn);
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
        return Zone.wrapIZone(Continent.getAll());
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/instance" + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return Zone.unwrapIZone(cacheMap.get(cacheKey));

        List<City> c = getInnerZones(conn)
                .stream()
                .flatMap(d -> {
                    try {
                        return d.getCities(conn).stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        cacheMap.put(cacheKey, Zone.wrapIZone(c));
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
