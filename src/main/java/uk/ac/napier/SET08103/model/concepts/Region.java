package uk.ac.napier.SET08103.model.concepts;

import uk.ac.napier.SET08103.model.concepts.types.PopulationInfo;
import uk.ac.napier.SET08103.model.concepts.zone.AbstractZone;
import uk.ac.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.model.concepts.zone.Zone;
import uk.ac.napier.SET08103.model.db.IFieldEnum;
import uk.ac.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Region extends AbstractZone implements IFieldEnum<String>, IDistributedPopulation {

    /**
     * Creates a Region instance using whatever is LIKE the name passed in. LIKE referring to SQL string matching.
     */
    public static Region fromName(final String name, final Connection conn) throws SQLException {
        try (final PreparedStatement stmt
                     = conn.prepareStatement(
                "SELECT DISTINCT(" + Country.REGION + "), " + Country.CONTINENT +
                        " FROM " + Country.TABLE + " WHERE " + Country.REGION + " LIKE ?")) {

            stmt.setString(1, "%" + name + "%");

            try (final ResultSet res = stmt.executeQuery()) {
                if (res.next()) {
                    return new Region(
                            res.getString(Country.REGION),
                            Continent.fromDatabaseString(res.getString(Country.CONTINENT)));
                }
                else
                    throw new IllegalArgumentException("No region with name: " + name);
            }
        }
    }

    private final String name;
    private final Continent continent;

    private Region(final String name, final Continent continent) {
        this.name = name;
        this.continent = continent;
    }

    @Override
    public List<City> getCities(final Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + continent + "/" + name + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return Zone.unwrapIZone(cacheMap.get(cacheKey));

        List<City> c = this.getInnerZones(conn)
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
    public List<IZone> getInnerZones(final Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + continent + "/" + name + "/innerZones";
        if (cacheMap.containsKey(cacheKey))
            return cacheMap.get(cacheKey);

        // Will always be unique because country is a db entity
        final PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT * FROM " + Country.TABLE +
                        " WHERE " + Country.REGION + " = ?"
        );
        stmt.setString(1, name);

        final List<IZone> countries = new ArrayList<>();
        try (stmt; final ResultSet res = stmt.executeQuery()) {
            while (res.next())
                countries.add(
                        Country.fromCountryCode(
                                res.getString(Country.PRIMARY_KEY), conn));
        }

        cacheMap.put(cacheKey, countries);

        return countries;
    }

    @Override
    public String getValue() {
        return this.name;
    }

    @Override
    public PopulationInfo getPopulationInfo(final Connection conn) throws SQLException {
        return new PopulationInfo(
                this,
                getTotalPopulation(conn),
                getInnerZones(conn).stream()
                        .map(i -> {
                            try {
                                return ((IDistributedPopulation)i)
                                        .getPopulationInfo(conn).inCities;
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }).reduce(0L, Long::sum)
        );
    }

    @Override
    public long getTotalPopulation(final Connection conn) throws SQLException {
        //SELECT country.Region, SUM(country.Population) AS Total
        //FROM country
        //WHERE CountryCode = ?
        //GROUP BY country.Region
        //ORDER BY country.Region

        try (final PreparedStatement ps = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT",
                            Country.REGION, ",",
                            "SUM(", Country.POPULATION, ") AS Total",
                        "FROM", Country.TABLE,
                        "WHERE", Country.REGION, "= ?",
                        "GROUP BY", Country.REGION
                )
        )) {
            ps.setString(1, name);

            try (final ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return res.getInt("Total");
                }
                else
                    throw new RuntimeException("No region with name: " + name);
            }
        }
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
