package com.napier.SET08103.model.concepts.types;

import com.napier.SET08103.model.concepts.zone.IZone;

public class PopulationInfo {

    public final IZone location;

    public final long total;
    public final long inCities;
    public final long outsideCities;

    public PopulationInfo(
            IZone location,
            long total,
            long inCities
    ) {
        this.location = location;
        this.total = total;
        this.inCities = inCities;
        this.outsideCities = total - inCities;
    }
}