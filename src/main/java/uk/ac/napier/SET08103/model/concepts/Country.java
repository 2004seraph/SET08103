package uk.ac.napier.SET08103.model.concepts;

import uk.ac.napier.SET08103.model.concepts.types.PopulationInfo;
import uk.ac.napier.SET08103.model.concepts.zone.AbstractZone;
import uk.ac.napier.SET08103.model.concepts.zone.IDistributedPopulation;
import uk.ac.napier.SET08103.model.concepts.zone.IZone;
import uk.ac.napier.SET08103.model.concepts.zone.Zone;
import uk.ac.napier.SET08103.model.db.IEntity;
import uk.ac.napier.SET08103.model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
     * @return A Country instance
     * @throws IllegalArgumentException if no country exists with the given code
     */
    public static Country fromCountryCode(final String countryCode, final Connection conn) throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM " + TABLE + " WHERE " + PRIMARY_KEY + " = ?");
        stmt.setString(1, countryCode);

        try (stmt; final ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                final Country newInstance = new Country(
                        countryCode,
                        Region.fromName(res.getString(REGION), conn),
                        res.getInt(CAPITAL),
                        conn);
                newInstance.name = res.getString(NAME);
                return newInstance;
            }
            else
                throw new IllegalArgumentException("No country with code: " + countryCode);
        }
    }

    /**
     * Returns a Country instance from a name. In the event of multiple matches, the country with the
     * higher population is chosen.
     * @return A Country instance
     * @throws IllegalArgumentException if no country is found with a name LIKE the one given
     */
    public static Country fromName(final String name, final Connection conn) throws SQLException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Invalid City name");

        final PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM " + TABLE +
                        " WHERE LOWER( Name ) LIKE ? ORDER BY " + POPULATION + " DESC"
        );
        stmt.setString(1, "%" + name.toLowerCase(Locale.ENGLISH) + "%");

        try (stmt; final ResultSet res = stmt.executeQuery()) {
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

    private Country(final String countryCode, final Region region, final int capitalId, final Connection conn)
            throws SQLException {
        this.countryCode = countryCode;
        this.region = region;
        this.continent = (Continent)region.getOuterZone();
        this.population = getTotalPopulation(conn);

        // Some "countries" have no capital, like Antarctica
        this.capital = (capitalId != NULL_CAPITAL) ?
                City.fromIdAsCapital(capitalId, this, conn) : null;
    }

    @Override
    public List<City> getCities(final Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + countryCode + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return Zone.unwrapIZone(cacheMap.get(cacheKey));

        final List<City> res = getInnerZones(conn)
                .stream()
                .flatMap(districts -> {
                    try {
                        final List<City> lowerLevelQuery = districts.getCities(conn);
                        return lowerLevelQuery.stream();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        cacheMap.put(cacheKey, Zone.wrapIZone(res));
        return res;
    }

    @Override
    public List<IZone> getInnerZones(final Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + countryCode + "/innerZones";
        if (cacheMap.containsKey(cacheKey))
            return cacheMap.get(cacheKey);

        final PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT " + City.DISTRICT + " FROM " + City.TABLE +
                        " WHERE " + City.COUNTRY_CODE + " = ?"
        );
        stmt.setString(1, countryCode);

        final List<IZone> districts = new ArrayList<>();

        try (stmt; final ResultSet res = stmt.executeQuery()) {
            boolean nullDistricts = false;

            while (res.next()) {
                final District district = District.fromName(res.getString(City.DISTRICT), this, conn);
                if (district != null) {
                    districts.add(district);
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
                final PreparedStatement nullDistrictStmt = conn.prepareStatement(
                        Model.buildStatement(
                                "SELECT * FROM", City.TABLE,
                                "WHERE", City.COUNTRY_CODE, "= ?", "AND",
                                City.DISTRICT, "= ?"
                        )
                );
                nullDistrictStmt.setString(1, countryCode);
                nullDistrictStmt.setString(2, District.nullFieldValue);

                try (nullDistrictStmt; final ResultSet nullDistrictRes = nullDistrictStmt.executeQuery()) {
                    while (nullDistrictRes.next()) {
                        districts.add(City.fromId(nullDistrictRes.getInt(City.PRIMARY_KEY), conn));
                    }
                }
            }
        }

        cacheMap.put(cacheKey, districts);

        return districts;
    }

    @Override
    public PopulationInfo getPopulationInfo(final Connection conn) throws SQLException {
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
    public long getTotalPopulation(final Connection conn) throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement(
                Model.buildStatement(
                        "SELECT", POPULATION, "FROM",
                        TABLE, "WHERE", PRIMARY_KEY, "= ?"
                )
        );
        stmt.setString(1, this.countryCode);

        try (stmt; final ResultSet res = stmt.executeQuery()) {
            if (res.next()) {
                return res.getInt(POPULATION);
            } else
                throw new RuntimeException("No entry found");
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
