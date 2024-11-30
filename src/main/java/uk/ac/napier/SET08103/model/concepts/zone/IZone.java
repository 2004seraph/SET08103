package uk.ac.napier.SET08103.model.concepts.zone;

import uk.ac.napier.SET08103.model.concepts.City;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Tree-style API where each Zone is a node
 */
public interface IZone extends Comparable<IZone> {
    // Tree
    Zone getZoneLevel();                                                    // Rank
    IZone getOuterZone();                                                   // Parent
    List<IZone> getInnerZones(final Connection conn) throws SQLException;   // Children

    // Domain specific
    List<City> getCities(final Connection conn) throws SQLException;
    long getTotalPopulation(final Connection conn) throws SQLException;
}
