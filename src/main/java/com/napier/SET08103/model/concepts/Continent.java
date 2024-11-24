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

public final class Continent extends AbstractZone implements IFieldEnum<Continent.Name>, IZone {

    /**
     * In the event of a name clash, it will return the continent with the higher population.
     * @param name The name of the continent
     * @return An instance of Continent
     */
    public static Continent fromName(String name) {
        return new Continent(Name.parse(name));
    }

    public enum Name {
        NORTH_AMERICA("North America"),
        EUROPE("Europe"),
        ASIA("Asia"),
        AFRICA("Africa"),
        OCEANIA("Oceania"),
        ANTARCTICA("Antarctica"),
        SOUTH_AMERICA("South America");

        public static final Name[] asList = values();

        private final String databaseName;

        Name(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getDatabaseName() {
            return this.databaseName;
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public static Name parse(String databaseName) {
            return Arrays.stream(Name.values())
                    .filter(c -> c.databaseName.equals(databaseName))
                    .findFirst().get();
        }

        @Override
        public String toString() {
            return databaseName;
        }
    }

    private final Name name;

    public Continent(Name name) {
        this.name = name;
    }

    @Override
    public Name getValue() {
        // Use .toString() on the return value to get a db compatible string.
        // I.e. NORTH_AMERICA -> "North America"
        return name;
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        return getInnerZones(conn)
                .stream()
                .flatMap(d -> {
                    try {
                        return d.getCities(conn).stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
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
