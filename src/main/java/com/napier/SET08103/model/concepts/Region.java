package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.IZone;
import com.napier.SET08103.model.PopulationInfo;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.SQLException;

public final class Region implements IFieldEnum<String>, IZone {

    public static Region fromName(String name) {
        return new Region(name);
    }

    private final String name;

    private Region(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return name;
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
    public Zone GetZoneLevel() {
        return Zone.REGIONS;
    }
}
