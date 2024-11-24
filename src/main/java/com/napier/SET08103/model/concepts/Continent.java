package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Continent extends AbstractZone implements IFieldEnum<Continent.FieldEnum>, IZone {

    public static Continent likeDatabaseString(String name, Connection conn) throws SQLException {
        // Select the continent with the higher population
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT SUM(" + Country.populationField + ") as Total, " + Country.continentField +
                        " FROM " + Country.table +
                        " WHERE LOWER( " + Country.continentField + " ) LIKE ? GROUP BY " +
                        Country.continentField + " ORDER BY Total" +
                        " DESC"
        );
        stmt.setString(1, "%" + name + "%");

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return Continent.fromDatabaseString(res.getString(Country.continentField));
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
                "SELECT DISTINCT " + Country.regionField + " FROM " + Country.table +
                        " WHERE " + Country.continentField + " = ?"
        );
        stmt.setString(1, name.getDatabaseName());

        List<IZone> regions = new ArrayList<>();
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next())
                regions.add(Region.fromName(res.getString(Country.regionField), conn));
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
    public PopulationInfo getPopulation() {
        return new PopulationInfo(
                this,
                0,
                0
        );
    }

    @Override
    public String toString() {
        return name.getDatabaseName();
    }
}
