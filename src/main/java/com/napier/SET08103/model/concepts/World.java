package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class World extends AbstractZone {
    public static final World instance = new World();

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
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        return Continent.getAll().stream().map(c -> (IZone)c).collect(Collectors.toList());
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        return List.of();
    }

    @Override
    public long getTotalPopulation(Connection conn) throws SQLException {
        return 0;
    }
}
