## Use Case  Generate country report

## CHARACTERISTIC INFORMATION
Goal in Context: 	Allow users to access population data for countries, organized in multiple ways such as by world, continent, or region population rankings.
Scop & Level:	Population Information System, Primary Task
Preconditions: 	The population database must be updated and organized by country, continent, and region.
Success End Condition: 	A list of countries with population data is generated, sorted based on the user-specified criteria.
Failed End Condition: 	No data is displayed or incorrect data is generated.
Primary Actor: User/Organization
Secondary Actor: Database
Trigger:	User selects a report type (global, continent, region) and optionally inputs the desired top N countries by population.

## MAIN SUCCESS SCENARIO 
1	User selects the option to generate a country report.
2	User indicates how they want the report to be organized (by world, region, language, etc.)
3	System retrieves the relevant population data from the database.
4	System organizes the countries based on the specified criteria (e.g. Largest to smallest population)
5	System generates and displays the report

## EXTENSIONS 
2a	User does not provide input for top N: System defaults to show all Countries. 
3a	Data retrieval error: System displays an error message and asks the user to retry.

## SUB-VARIATIONS 
Report can be organized by:
- World population rankings
- Continent population rankings
- Region population rankings
- Top N countries globally, by continent, or by region

## RELATED INFORMATION 
Priority: High
Performance: Report generation should take no longer than 5 seconds after user input
Frequency: generated daily by users
Channel to actors:	Web interface with a connection to the population database

## OPEN ISSUES
real-time updates for population data to maintain report accuracy

## SCHEDULE
Due Date: 27/11/2024

