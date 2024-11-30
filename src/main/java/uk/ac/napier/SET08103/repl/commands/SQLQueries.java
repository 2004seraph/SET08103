package uk.ac.napier.SET08103.repl.commands;

public final class SQLQueries {

    // Prevents instantiation, i.e. writing `SQLQueries sq = new SQLQueries();`
    private SQLQueries() { }

    // This function generates countries largest population to smallest.
    public static String world_countries_largest_population_to_smallest(){
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "ORDER BY population DESC";
    }

    // This function generates countries from a region ordered largest population to smallest.
    public static String countries_in_region_largest_population_to_smallest(String region){
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "WHERE region = " + region
                + "ORDER BY population DESC";
    }

    //This function generates countries from largest to smallest within a continent. User can input continent
    public static String Cities_in_a_continent_organised_by_largest_population_to_smallest(String continent){
        return "Select code, city, population, country, continent"
                + "FROM world"
                + "WHERE continent = '" + continent + "' "
                + "ORDER BY population DESC";
    }

    //This function generates top N populated countries in the world. User can input a desired number
    public static String top_n_populated_Countries(int n) {
        return "SELECT code, name, continent, region, population, capital "
                + "FROM country "
                + "ORDER BY population DESC "
                + "LIMIT " + n;
    }
    //This function generates total population of the entire world
    public static String world_population() {
        return "SELECT SUM(population) AS world_population FROM country";
    }

    //This function generates total population of a continent
    public static String continent_population(String continent) {
        return "SELECT SUM(population) AS continent_population "
                + "FROM country "
                + "WHERE continent = '" + continent + "'";
    }

    //This function generates total population of a region
    public static String region_population(String region) {
        return "SELECT SUM(population) AS region_population "
                + "FROM country "
                + "WHERE region = '" + region + "'";
    }

    // This function generates cities largest population to smallest
    public static String cities_in_world_largest_population_to_smallest(){
        return "SELECT ci.ID "
                + "FROM world.city ci "
                + "ORDER BY population DESC";
    }

    // This function generates cities in a country largest population to smallest
    public static String cities_in_a_country_largest_population_to_smallest(String country){
        return "SELECT ci.ID "
                + "FROM world.city ci JOIN world.country co ON co.Code = ci.CountryCode "
                + "WHERE co.Name = '" + country + "' "
                + "ORDER BY ci.population DESC";
    }

    // This function generates cities in a region largest population to smallest
    public static String cities_in_a_region_largest_population_to_smallest(String region){
        return "SELECT ci.ID "
                + "FROM world.city ci JOIN world.country co ON co.Code = ci.CountryCode "
                + "WHERE co.Region = '" + region + "' "
                + "ORDER BY ci.population DESC";
    }

    // This function generates cities in a district largest population to smallest
    public static String cities_in_a_district_largest_population_to_smallest(String district){
        return "SELECT ci.ID "
                + "FROM world.city ci JOIN world.country co ON co.Code = ci.CountryCode "
                + "WHERE ci.District = '" + district + "' "
                + "ORDER BY ci.population DESC";
    }

//This function generates total population of the world
    public static String country_population(String countryName) {
        return "SELECT population AS country_population "
                + "FROM country "
                + "WHERE name = '" + countryName + "'";
    }

//This function generates total population of a district
public static String district_population(String districtName) {
    return "SELECT SUM(population) AS district_population "
            + "FROM city "
            + "WHERE district = '" + districtName + "'";
}

//This function generates total population of a city
public static String cityPopulation(String cityName) {
    return "SELECT population AS city_population "
            + "FROM city "
            + "WHERE name = '" + cityName + "'";
}
//The population of people, people living in cities, and people not living in cities in each country
    public static String populationBreakdownByCountry() {
        return "SELECT "
                + "country.name AS country_name, "
                + "country.population AS total_population, "
                + "IFNULL(SUM(city.population), 0) AS city_population, "
                + "(country.population - IFNULL(SUM(city.population), 0)) AS non_city_population "
                + "FROM country "
                + "LEFT JOIN city ON country.code = city.countrycode "
                + "GROUP BY country.name, country.population";
    }

}