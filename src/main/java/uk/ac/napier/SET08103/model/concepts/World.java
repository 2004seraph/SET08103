package uk.ac.napier.SET08103.model.concepts;

import uk.ac.napier.SET08103.model.concepts.zone.AbstractZone;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.model.concepts.zone.Zone;
import uk.ac.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Root of the tree of zones
 */
public final class World extends AbstractZone {
    public static final World INSTANCE = new World();

    private World() { }

    @Override
    public Zone getZoneLevel() {
        return Zone.WORLD;
    }

    @Override
    public IZone getOuterZone() {
        return null;
    }

    @Override
    public List<IZone> getInnerZones(final Connection conn) {
        return Zone.wrapIZone(Continent.getAll());
    }

    @Override
    public List<City> getCities(final Connection conn) {
        final String cacheKey = this.getClass().getName() + "/instance" + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return Zone.unwrapIZone(cacheMap.get(cacheKey));

        final List<City> c = getInnerZones(conn)
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
    public long getTotalPopulation(final Connection conn) throws SQLException {
        try (final PreparedStatement stmt = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT",
                        "SUM(", Country.POPULATION, ") AS Total",
                        "FROM", Country.TABLE
                )
        )) {
            try (final ResultSet res = stmt.executeQuery()) {
                if (res.next()) {
                    return res.getLong("Total");
                }
                else
                    throw new RuntimeException("Database error");
            }
        }
    }

    @Override
    public String toString() {
        return "the world";
    }
}
