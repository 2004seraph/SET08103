package com.napier.SET08103.model.concepts;

import com.napier.SET08103.model.IZone;
import com.napier.SET08103.model.Zone;
import com.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.SQLException;

public final class Continent implements IFieldEnum<Continent.ContinentName>, IZone {

    /**
     * In the event of a name clash, it will return the continent with the higher population.
     * @param name The name of the continent
     * @return An instance of Continent
     */
    public static Continent fromName(String name) {
        return new Continent(ContinentName.valueOf(name));
    }

    public enum ContinentName {
        NORTH_AMERICA,
        EUROPE,
        ASIA,
        AFRICA,
        OCEANIA,
        ANTARCTICA,
        SOUTH_AMERICA;

        public static final ContinentName[] asList = values();
    }

    private final ContinentName name;

    public Continent(ContinentName name) {
        this.name = name;
    }

    @Override
    public ContinentName getValue() {
        // Use .toString() on the return value to get a db compatible string.
        // I.e. NORTH_AMERICA -> "North America"
        return name;
    }

    @Override
    public Zone GetZoneLevel() {
        return Zone.CONTINENTS;
    }

    @Override
    public int getPopulation() {
        return 0;
    }
}
