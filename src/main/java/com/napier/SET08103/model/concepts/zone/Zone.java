package com.napier.SET08103.model.concepts.zone;

public enum Zone {
    WORLD,
    CONTINENTS,
    REGIONS,
    COUNTRIES,
    DISTRICTS,
    CITIES,
    CAPITALS;

    public static final Zone[] asList = values();

    // Each enum has an integer value associated with it to create the notion
    // of one being bigger or "encompassing" the other. (The WORLD is >>> than a REGION)

    // This makes error checking easier, we can prevent someone looking up the biggest
    // CONTINENT in a CITIES with a simple number comparison.

    public int getSizeRank() {
        return 1 << (7 - this.ordinal());
    }
}
