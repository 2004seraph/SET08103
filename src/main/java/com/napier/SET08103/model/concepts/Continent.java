package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.types.PopulationInfo;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.model.db.IFieldEnum;
import com.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Continent type from the database
 */
public final class Continent extends AbstractZone implements IFieldEnum<Continent.FieldEnum>, IDistributedPopulation {

    public static Continent likeDatabaseString(String name, Connection conn) throws SQLException {
        // Select the continent with the higher population
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT SUM(" + Country.POPULATION + ") as Total, " + Country.CONTINENT +
                        " FROM " + Country.TABLE +
                        " WHERE LOWER( " + Country.CONTINENT + " ) LIKE ? GROUP BY " +
                        Country.CONTINENT + " ORDER BY Total" +
                        " DESC"
        );
        stmt.setString(1, "%" + name + "%");

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return Continent.fromDatabaseString(res.getString(Country.CONTINENT));
            } else
                throw new IllegalArgumentException("No Continent with name like: " + name);
        }
    }

    public static Continent fromDatabaseString(String name) {
        return new Continent(FieldEnum.parse(name));
    }

    public static Continent fromValue(FieldEnum value) {
        return new Continent(value);
    }

    public static Continent valueOf(String name) {
        return new Continent(FieldEnum.valueOf(name));
    }

    public enum FieldEnum {
        NORTH_AMERICA("North America"),
        EUROPE("Europe"),
        ASIA("Asia"),
        AFRICA("Africa"),
        OCEANIA("Oceania"),
        ANTARCTICA("Antarctica"),
        SOUTH_AMERICA("South America");

        public static final FieldEnum[] asList = values();

        private final String databaseName;

        FieldEnum(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getDatabaseName() {
            return this.databaseName;
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public static FieldEnum parse(String databaseName) {
            return Arrays.stream(FieldEnum.values())
                    .filter(c -> c.databaseName.equalsIgnoreCase(databaseName))
                    .findFirst().get();
        }

        @Override
        public String toString() {
            return databaseName;
        }
    }

    public static List<Continent> getAll() {
        return Arrays.stream(FieldEnum.asList).map(Continent::fromValue).collect(Collectors.toList());
    }

    private final FieldEnum name;

    public Continent(FieldEnum name) {
        this.name = name;
    }

    @Override
    public FieldEnum getValue() {
        // Use .toString() on the return value to get a db compatible string.
        // I.e. NORTH_AMERICA -> "North America"
        return name;
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + name + "/cities";
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
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + name + "/innerZones";
        if (cacheMap.containsKey(cacheKey))
            return cacheMap.get(cacheKey);

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT " + Country.REGION + " FROM " + Country.TABLE +
                        " WHERE " + Country.CONTINENT + " = ?"
        );
        stmt.setString(1, name.getDatabaseName());

        List<IZone> regions = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                regions.add(Region.fromName(res.getString(Country.REGION), conn));
        }

        cacheMap.put(cacheKey, regions);
        return regions;
    }

    @Override
    public Zone getZoneLevel() {
        return Zone.CONTINENTS;
    }

    @Override
    public IZone getOuterZone() {
        return null;
    }

    @Override
    public PopulationInfo getPopulationInfo(Connection conn) throws SQLException {
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
    public long getTotalPopulation(Connection conn) throws SQLException {
//        SELECT country.Continent , SUM(country.Population) AS Total
//        FROM country
//        GROUP BY country.Continent
//        ORDER BY country.Continent
        try (PreparedStatement ps = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT",
                        Country.CONTINENT, ",",
                        "SUM(", Country.POPULATION, ") AS Total",
                        "FROM", Country.TABLE,
                        "WHERE", Country.CONTINENT, "= ?"
                )
        )) {
            ps.setString(1, name.getDatabaseName());

            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    return res.getLong("Total");
                }
                else
                    throw new InternalError("No continent with name: " + name);
            }
        }
    }

    @Override
    public String toString() {
        return name.getDatabaseName();
    }
}
