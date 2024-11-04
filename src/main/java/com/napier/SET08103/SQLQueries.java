package com.napier.SET08103;

public class SQLQueries {
    public static String world_countries_largest_population_to_smallest(){
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "ORDER BY population DESC";
    }

    public static String countries_in_region_largest_population_to_smallest(String region){
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "WHERE region = " + region
                + "ORDER BY population DESC";
    }
}