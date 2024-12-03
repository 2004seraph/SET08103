package uk.ac.napier.SET08103.model.concepts.types;

import uk.ac.napier.SET08103.model.concepts.zone.IZone;

import java.sql.Connection;
import java.sql.SQLException;

public final class PopulationInfo implements Comparable<PopulationInfo> {
    public final IZone location;

    public final long total;
    public final long inCities;
    public final long outsideCities;

    public PopulationInfo(
            final IZone location,
            long total,
            final long inCities
    ) {
        this.location = location;
        this.total = total;
        this.inCities = inCities;
        this.outsideCities = total - inCities;
    }

    /**
     * Prints the column headers for a population report
     */
    public static void printHeaders() {
        System.out.printf(
                "%-45s %22s | %18s %5s | %18s %5s |",
                "Name", "Total Population", "Urban Population", "%", "Rural Population", "%");
        System.out.println();
    }

    /**
     * Outputs this instance's data to the console in table format, for a population report
     */
    public void print(final Connection conn) throws SQLException {
        System.out.printf(
                "%-45s %,22d | %,18d %5s | %,18d %5s |",

                location.toString(),
                location.getTotalPopulation(conn),
                this.inCities, "(" + Math.round(((double)this.inCities / (double)this.total) * 100D) + "%)",
                this.outsideCities, "(" + Math.round(((double)this.outsideCities / (double)this.total) * 100D) + "%)");
        System.out.println();
    }

    @Override
    public boolean equals(final Object other) {
        if ((other == null) || !(other.getClass().isInstance(this)))
            return false;

        return this.location.equals(((PopulationInfo)other).location);
    }

    @Override
    public int compareTo(final PopulationInfo o) {
        return this.location.compareTo(o.location);
    }
}