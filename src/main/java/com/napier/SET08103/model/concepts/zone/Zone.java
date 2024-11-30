package com.napier.SET08103.model.concepts.zone;

import java.util.List;
import java.util.stream.Collectors;

public enum Zone {
    WORLD,
    CONTINENTS,
    REGIONS,
    COUNTRIES,
    DISTRICTS,
    CITIES,
    CAPITALS;

    @SuppressWarnings("unused")
    public static final Zone[] asArray = values();

    /**
     * Converts a List<City or Continent or Country, etc> to a LIst<IZone>
     * @param concreteIZones List of a zone type
     * @return All collapsed to the IZone type
     * @param <T> City, District, Country, Region, Continent, or World
     */
    public static <T> List<IZone> wrapIZone(final List<T> concreteIZones) {
        return concreteIZones.stream().map(c -> (IZone)c).collect(Collectors.toList());
    }

    /**
     * Converts a List<IZone> to a given target type (Zone)
     * @param concreteIZones list of IZones
     * @return whatever type will work here
     * @param <T> City, District, Country, Region, Continent, or World
     */
    @SuppressWarnings("unchecked")
    public static <T extends IZone> List<T> unwrapIZone(final List<IZone> concreteIZones) {
        return concreteIZones.stream().map(c -> (T)c).collect(Collectors.toList());
    }

    /**
     * The "bigness" of this instance, for example WORLD is bigger than DISTRICTS
     */
    public int getSizeRank() {
        // Each enum has an integer value associated with it to create the notion
        // of one being bigger or "encompassing" the other. (The WORLD is >>> than a REGION)

        // This makes error checking easier, we can prevent someone looking up the biggest
        // CONTINENT in a CITIES with a simple number comparison.
        return 1 << (7 - this.ordinal());
    }
}
