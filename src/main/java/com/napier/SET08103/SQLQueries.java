package com.napier.SET08103;

public class SQLQueries {
    // This sql query produces countries largest population to smallest.
    public static String world_countries_largest_population_to_smallest(){
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "ORDER BY population DESC";
    }

    // This sql query produces countries from a region ordered largest population to smallest.
    public static String countries_in_region_largest_population_to_smallest(String region){
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "WHERE region = " + region
                + "ORDER BY population DESC";
    }

    //This sql query produces countries from largest to smallest within a continent. User can input continent
    public static String Cities_in_a_continent_organised_by_largest_population_to_smallest(String continent){
        return "Select code, city, population, country, continent"
                + "FROM world"
                + "WHERE continent = '" + continent + "' "
                + "ORDER BY population DESC";
    }

    //This sql query produces top N populated countries in the world. User can input a desired number
    public static String top_n_populated_Countries(int n) {
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "ORDER BY population DESC "
                + "LIMIT " + n;
    }


}