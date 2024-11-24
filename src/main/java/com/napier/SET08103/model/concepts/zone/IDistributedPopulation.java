package com.napier.SET08103.model.concepts.zone;

import com.napier.SET08103.model.PopulationInfo;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDistributedPopulation {
    public PopulationInfo getPopulationInfo(Connection conn) throws SQLException;
}
