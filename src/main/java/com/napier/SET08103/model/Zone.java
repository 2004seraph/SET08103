package com.napier.SET08103.model;

// Used for supplying "search terms" to report functions (
// for example writing Zone.Select.WORLD as a function argument)
public enum Zone {
    WORLD(0),
    CONTINENTS(1),
    REGIONS(2),
    COUNTRIES(3),
    DISTRICTS(4),
    CITIES(5),
    CAPITALS(6);

    // Each enum has an integer value associated with it to create the notion
    // of one being bigger or "encompassing" the other. (The WORLD is >>> than a REGION)

    // This makes error checking easier, we can prevent someone looking up the biggest
    // CONTINENT in a CITIES with a simple number comparison.

    // The code below merely implements the storage of the above.
    public final int sizeFlag;

    private Zone(int size) {
        this.sizeFlag = 1 << size; // binary shift leftwards
    }

    public int getSizeRank() {
        return sizeFlag;
    }
}
