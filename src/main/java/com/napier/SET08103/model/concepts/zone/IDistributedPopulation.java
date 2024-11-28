package com.napier.SET08103.model.concepts.zone;

import com.napier.SET08103.model.concepts.types.PopulationInfo;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a zone that can have rich population statistics, one with different population
 * demographics, i.e. those in cities and those outside cities
 */
public interface IDistributedPopulation {
    public PopulationInfo getPopulationInfo(Connection conn) throws SQLException;
}
