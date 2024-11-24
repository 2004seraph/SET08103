package com.napier.SET08103.model;

import com.napier.SET08103.model.concepts.zone.IZone;

public class PopulationInfo {

    public final IZone location;

    public final int total;
    public final int inCities;
    public final int outsideCities;

    public PopulationInfo(
            IZone location,
            int total,
            int inCities
    ) {
        this.location = location;
        this.total = total;
        this.inCities = inCities;
        this.outsideCities = total - inCities;
    }
}