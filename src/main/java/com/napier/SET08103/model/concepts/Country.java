package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.concepts.zone.AbstractZone;
import com.napier.SET08103.model.concepts.zone.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IEntity;
import com.napier.SET08103.model.db.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Country extends AbstractZone implements IEntity, IZone {

    // no spelling mistakes
    public static final String table = "country";
    public static final String primaryKeyField = table + ".Code";
    public static final String populationField = table + ".Population";
    public static final String capitalField = table + ".Capital";
    public static final String regionField = table + ".Region";
    public static final String continentField = table + ".Continent";
    public static final String nameField = table + ".Name";

    public static Country fromCountryCode(String countryCode, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + table + " WHERE " + primaryKeyField + " = ?");
        ps.setString(1, countryCode);
        ResultSet res = ps.executeQuery();

        if (res.next()) {
            Country c = new Country(countryCode, Region.fromName(res.getString(regionField), conn));
            c.name = res.getString(nameField);
            return c;
        }
        else
            throw new IllegalArgumentException("No country with code: " + countryCode);
    }

    // Primary key column: "Code"
    public final String countryCode;

    public final Region region;
    public final Continent continent;

    public String name;
    public int population;
    public String capital;

    private Country(String countryCode, Region region) {
        this.countryCode = countryCode;
        this.region = region;
        this.continent = (Continent)region.getOuterZone();
    }

    @Override
    public List<City> getCities(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + countryCode + "/cities";
        if (cacheMap.containsKey(cacheKey))
            return unwrapIZone(cacheMap.get(cacheKey));

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

        cacheMap.put(cacheKey, wrapIZone(c));
        return c;
    }

    @Override
    public List<IZone> getInnerZones(Connection conn) throws SQLException {
        final String cacheKey = this.getClass().getName() + "/" + countryCode + "/innerZones";
        if (cacheMap.containsKey(cacheKey))
            return cacheMap.get(cacheKey);

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT " + City.districtField + " FROM " + City.table +
                        " WHERE " + City.countryCodeField + " = ?"
        );
        stmt.setString(1, countryCode);

        List<IZone> districts = new ArrayList<>();
        boolean nullDistricts = false;
        try (stmt; ResultSet res = stmt.executeQuery()) {
            while (res.next()) {
                District d = District.fromName(res.getString(City.districtField), this, conn);
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
                                "SELECT * FROM", City.table,
                                "WHERE", City.countryCodeField, "= ?", "AND",
                                City.districtField, "= ?"
                        )
                );
                nullDistrictStmt.setString(1, countryCode);
                nullDistrictStmt.setString(2, District.nullFieldValue);

                try (nullDistrictStmt; ResultSet nullDistrictRes = nullDistrictStmt.executeQuery()) {
                    while (nullDistrictRes.next()) {
                        City c = City.fromId(nullDistrictRes.getInt(City.primaryKeyField), conn);
                        districts.add(c);
                    }
                }
            }
        }

        cacheMap.put(cacheKey, districts);

        return districts;
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
