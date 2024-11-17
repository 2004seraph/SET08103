package com.napier.SET08103.model;

import java.sql.Connection;
import java.sql.SQLException;

public interface IZone {
    public int getPopulation(Connection conn) throws SQLException;

    public Zone GetZoneLevel();
}
