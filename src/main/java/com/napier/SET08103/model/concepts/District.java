package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.IZone;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class District implements IFieldEnum<String>, IZone {

    public static final String districtFieldName = "District";

    public static District fromName(String name) {
        return new District(name);
    }

    private final String name;

    private District(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return name;
    }

    @Override
    public int getPopulation() {
//        try (PreparedStatement stmt = conn.prepareStatement(
//                "SELECT SUM(" +  + ") FROM ? WHERE ? = ?",
//                new String[] {
//                        City.populationFieldName,
//                        City.tableName,
//                        districtFieldName,
//                        name
//                });
//            ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next())
//                    throw new InternalError("No population results for District: " + name);
//
//                return rs.getInt(1);
//            }
        return 0;
    }

    @Override
    public Zone GetZoneLevel() {
        return Zone.DISTRICTS;
    }
}
