package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.types.PopulationInfo;
import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.concepts.zone.Zone;
import com.napier.SET08103.model.db.IEntity;
import com.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Country entity from the database
 */
public final class Country extends AbstractZone implements IEntity, IDistributedPopulation {

    // no spelling mistakes
    public static final String TABLE = "country";
    public static final String PRIMARY_KEY = TABLE + ".Code";
    public static final String POPULATION = TABLE + ".Population";
    public static final String CAPITAL = TABLE + ".Capital";
    public static final String REGION = TABLE + ".Region";
    public static final String CONTINENT = TABLE + ".Continent";
    public static final String NAME = TABLE + ".Name";

    public static final int NULL_CAPITAL = 0;

    /**
     * Returns a Country instance from a given primary key
     * @param countryCode "USA"
     * @param conn
     * @return A Country instance
     * @throws SQLException
     */
    public static Country fromCountryCode(String countryCode, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + TABLE + " WHERE " + PRIMARY_KEY + " = ?");
        ps.setString(1, countryCode);
        ResultSet res = ps.executeQuery();

        if (res.next()) {
            Country c = new Country(
                    countryCode,
                    Region.fromName(res.getString(REGION), conn),
                    res.getInt(CAPITAL),
                    conn);
            c.name = res.getString(NAME);
            return c;
        }
        else
            throw new IllegalArgumentException("No country with code: " + countryCode);
    }

    /**
     * Returns a Country instance from a name. In the event of multiple matches, the country with the
     * higher population is chosen.
     * @param countryCode "USA"
     * @param conn
     * @return A Country instance
     * @throws SQLException
     */
    public static Country fromName(String name, Connection conn) throws SQLException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Invalid City name");

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM " + TABLE +
                        " WHERE LOWER( Name ) LIKE ? ORDER BY " + POPULATION + " DESC"
        );
        stmt.setString(1, "%" + name.toLowerCase() + "%");

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return fromCountryCode(res.getString(PRIMARY_KEY), conn);
            } else
                throw new IllegalArgumentException("No city with name: " + name);
        }
    }

    // Primary key column: "Code"
    public final String countryCode;
    private String name;

    public final Region region;
    public final Continent continent;
    public final long population;
    public final City capital;

    private Country(String countryCode, Region region, int capitalId, Connection conn) throws SQLException {
        this.countryCode = countryCode;
        this.region = region;
        this.continent = (Continent)region.getOuterZone();
        this.population = getTotalPopulation(conn);

        // Some "countries" have no capital, like Antarctica
        this.capital = (capitalId != NULL_CAPITAL) ? City.fromIdAsCapital(capitalId, this, conn) : null;
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + countryCode + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return Zone.unwrapIZone(cacheMap.get(cacheKey));

        List<City> c = getInnerZones(conn)
                .stream()
                .flatMap(d -> {
                    try {
                        List<City> d2 = d.getCities(conn);
                        return d2.stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        cacheMap.put(cacheKey, Zone.wrapIZone(c));
        return c;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + countryCode + "/innerZones";
        if (cacheMap.containsKey(cacheKey))
            return cacheMap.get(cacheKey);

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT " + City.DISTRICT + " FROM " + City.TABLE +
                        " WHERE " + City.COUNTRY_CODE + " = ?"
        );
        stmt.setString(1, countryCode);

        List<IZone> districts = new ArrayList<>();
        boolean nullDistricts = false;
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
                District d = District.fromName(res.getString(City.DISTRICT), this, conn);
                if (d != null) {
                    districts.add(d);
                } else {
                    nullDistricts = true;
                }
            }

            // shunt all the cities with a null district onto the end of the list
            // doing this after the loop ensures no duplicate entries

            // this case is covered by test:
            //      ContinentIntegrationTest.zoneInfo:131->lambda$zoneInfo$8:124 expected: <581> but was: <578>
            // if the entire "if" statement is commented
            if (nullDistricts) {
                PreparedStatement nullDistrictStmt = conn.prepareStatement(
                        Model.buildStatement(
                                "SELECT * FROM", City.TABLE,
                                "WHERE", City.COUNTRY_CODE, "= ?", "AND",
                                City.DISTRICT, "= ?"
                        )
                );
                nullDistrictStmt.setString(1, countryCode);
                nullDistrictStmt.setString(2, District.nullFieldValue);

                try (nullDistrictStmt; ResultSet nullDistrictRes = nullDistrictStmt.executeQuery()) {
                    while (nullDistrictRes.next()) {
                        City c = City.fromId(nullDistrictRes.getInt(City.PRIMARY_KEY), conn);
                        districts.add(c);
                    }
                }
            }
        }

        cacheMap.put(cacheKey, districts);

        return districts;
    }

    @Override
    public PopulationInfo getPopulationInfo(Connection conn) throws SQLException {
        return new PopulationInfo(
                this,
                getTotalPopulation(conn),

                // Sum of district populations (which are made up only of cities
                getInnerZones(conn).stream().mapToLong(d -> {
                    try {
                        return d.getTotalPopulation(conn);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).reduce(0, Long::sum)
        );
    }

    @Override
    public long getTotalPopulation(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT", POPULATION, "FROM",
                        TABLE, "WHERE", PRIMARY_KEY, "= ?"
                )
        );
        stmt.setString(1, this.countryCode);

        try (stmt; ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return res.getInt(POPULATION);
            } else
                throw new InternalError("No entry found");
        }
    }

    @Override
    public Zone getZoneLevel() {
        return Zone.COUNTRIES;
    }

    @Override
    public IZone getOuterZone() {
        return region;
    }

    @Override
    public String getPrimaryKey() {
        return this.countryCode;
    }

    @Override
    public String toString() {
        return name;
    }
}
