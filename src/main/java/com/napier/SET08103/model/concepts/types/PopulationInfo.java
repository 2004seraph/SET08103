package com.napier.SET08103.model.concepts.types;

import com.napier.SET08103.model.concepts.zone.IZone;

import java.sql.Connection;
import java.sql.SQLException;

public class PopulationInfo implements Comparable<PopulationInfo> {
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

    public static void printHeaders() {
        System.out.printf(
                "%-45s %-22s %-22s %-22s",
                "Name", "Total Population", "Urban Population", "Rural Population");
        System.out.println();
    }

    public void print(Connection conn) throws SQLException {
        System.out.printf(
                "%-45s %-22s %-22s %-22s",

                location.toString(),
                location.getTotalPopulation(conn),
                this.inCities + " (" + Math.round(((double)this.inCities / (double)this.total) * 100D) + "%)",
                this.outsideCities + " (" + Math.round(((double)this.outsideCities / (double)this.total) * 100D) + "%)");
        System.out.println();
    }

    @Override
    public boolean equals(final Object other) {
        if ((other == null) || !(other.getClass().isInstance(this)))
            return false;

        return this.location.equals(((PopulationInfo)other).location);
    }

    @Override
    public int compareTo(PopulationInfo o) {
        return this.location.compareTo(o.location);
    }
}