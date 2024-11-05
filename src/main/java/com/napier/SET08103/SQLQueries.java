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

    public static String Cities_in_a_continent_organised_by_largest_population_to_smallest(String continent){
        return "Select code, city, population, country, continent"
                + "FROM world"
                + "WHERE continent = '" + continent + "' "
                + "ORDER BY population DESC";
    }

    public static String top_n_populated_Countries(int n) {
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "ORDER BY population DESC "
                + "LIMIT " + n;
    }


}