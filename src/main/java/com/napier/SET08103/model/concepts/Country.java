package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.IZone;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Country implements IEntity, IZone {

    public static final String tableName = "country";
    public static final String primaryKeyFieldName = "Code";
    public static final String populationFieldName = "Population";

    public static Country fromCountryCode(String countryCode, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM " + tableName + " where " + primaryKeyFieldName + " = ?");
        ps.setString(1, countryCode);
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return new Country(countryCode);
        else
            throw new IllegalArgumentException("No country with code: " + countryCode);
    }

    // Primary key column: "Code"
    private final String countryCode;

    public String name;
    public String continent;
    public String region;
    public int population;
    public String capital;

    private Country(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public int getPopulation(Connection conn) throws SQLException {
        return 0;
    }

    @Override
    public Zone GetZoneLevel() {
        return Zone.COUNTRIES;
    }

    @Override
    public String getPrimaryKey() {
        return this.countryCode;
    }
}
