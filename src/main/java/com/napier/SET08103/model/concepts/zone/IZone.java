package com.napier.SET08103.model.concepts.zone;

import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.concepts.City;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IZone {
    public PopulationInfo getPopulation();

    public Zone getZoneLevel();
    public IZone getOuterZone();

    public List<IZone> getInnerZones(Connection conn) throws SQLException;
    List<City> getCities(Connection conn) throws SQLException;
}
