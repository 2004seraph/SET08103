# SET08103 - Software Engineering Methods

[![ENU Badge](https://img.shields.io/badge/Edinburgh%20Napier%20University-CC002A)](https://www.napier.ac.uk/)

## Team 3 Project Repository

[![GitHub Workflow Status (master)](https://img.shields.io/github/actions/workflow/status/2004seraph/SET08103/docker-image.yml?branch=master&label=master)](https://github.com/2004seraph/SET08103/actions?query=branch%3Amaster)
[![GitHub Workflow Status (develop)](https://img.shields.io/github/actions/workflow/status/2004seraph/SET08103/docker-image.yml?branch=develop&label=develop)](https://github.com/2004seraph/SET08103/actions?query=branch%3Adevelop)
[![LICENSE](https://img.shields.io/github/license/2004seraph/SET08103.svg?style=flat-square)](https://github.com/2004seraph/SET08103/blob/master/LICENSE)
[![Releases](https://img.shields.io/github/release/2004seraph/SET08103/all.svg?style=flat-square)](https://github.com/2004seraph/SET08103/releases)
[![codecov](https://codecov.io/gh/2004seraph/SET08103/branch/master/graph/badge.svg?token=EDH24ELB68)](https://codecov.io/gh/2004seraph/SET08103)

## Usage

> [!IMPORTANT]
> You may want to read this if you're an examiner.

These commands assume a Linux environment with Docker and Maven installed.

1. Package the app: `mvn -DskipTests package`
2. Start the database Docker container using a config which allows the app to be run outside it's Docker container: 

   `docker compose -f docker-compose.yml -f docker-compose.dev.linux.yml up db`

    Or if you want to run on a Windows environment:    

   `docker compose -f docker-compose.yml -f docker-compose.windows.linux.yml up db`
3. Open an interactive REPL prompt: `MYSQL_ROOT_PASSWORD=root java -jar target/pop.jar` (ensuring zero arguments)
4. Ask for help with the commands: `help`
5. Run your query and see the output! You will be able to fulfil any requirement (and more!) using the commands in the prompt.

## Requirements

> [!NOTE]
> **31** requirements of **32** have been implemented, which is **96.9%**.

### Evidence

Very long results have been reduced to the beginning and ending dozen or so. The command has been included for you to reproduce it yourself.

| ID | Name                                                                                                                                                     | Met | Screenshot                                                                                                                |
|----|----------------------------------------------------------------------------------------------------------------------------------------------------------|-----|---------------------------------------------------------------------------------------------------------------------------|
| 1  | All the countries in the world organised by largest population to smallest.                                                                              | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)<br/>![Screenshot of terminal](./doc/demo/requirements/1.2.png) |
| 2  | All the countries in a continent organised by largest population to smallest.                                                                            | Yes | ![Screenshot of terminal](./doc/demo/requirements/2.1.png)<br/>![Screenshot of terminal](./doc/demo/requirements/2.2.png) |
| 3  | All the countries in a region organised by largest population to smallest.                                                                               | Yes | ![Screenshot of terminal](./doc/demo/requirements/3.png)                                                                  |
| 4  | The top `N` populated countries in the world where `N` is provided by the user.                                                                          | Yes | ![Screenshot of terminal](./doc/demo/requirements/4.png)                                                                  |
| 5  | The top `N` populated countries in a continent where `N` is provided by the user.                                                                        | Yes | ![Screenshot of terminal](./doc/demo/requirements/5.png)                                                                  |
| 6  | The top `N` populated countries in a region where `N` is provided by the user.                                                                           | Yes | ![Screenshot of terminal](./doc/demo/requirements/6.png)                                                                  |
| 7  | All the cities in the world organised by largest population to smallest.                                                                                 | Yes | ![Screenshot of terminal](./doc/demo/requirements/7.1.png)<br/>![Screenshot of terminal](./doc/demo/requirements/7.2.png) |
| 8  | All the cities in a continent organised by largest population to smallest.                                                                               | Yes | ![Screenshot of terminal](./doc/demo/requirements/8.1.png)<br/>![Screenshot of terminal](./doc/demo/requirements/8.2.png) |
| 9  | All the cities in a region organised by largest population to smallest.                                                                                  | Yes | ![Screenshot of terminal](./doc/demo/requirements/9.1.png)<br/>![Screenshot of terminal](./doc/demo/requirements/9.2.png) |
| 10 | All the cities in a country organised by largest population to smallest.                                                                                 | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 11 | All the cities in a district organised by largest population to smallest.                                                                                | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 12 | The top `N` populated cities in the world where `N` is provided by the user.                                                                             | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 13 | The top `N` populated cities in a continent where `N` is provided by the user.                                                                           | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 14 | The top `N` populated cities in a region where `N` is provided by the user.                                                                              | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 15 | The top `N` populated cities in a country where `N` is provided by the user.                                                                             | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 16 | The top `N` populated cities in a district where `N` is provided by the user.                                                                            | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 17 | All the capital cities in the world organised by largest population to smallest.                                                                         | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 18 | All the capital cities in a continent organised by largest population to smallest.                                                                       | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 19 | All the capital cities in a region organised by largest to smallest.                                                                                     | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 20 | The top `N` populated capital cities in the world  where `N` is provided by the user.                                                                    | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 21 | The top `N` populated capital cities in a continent where `N` is provided by the user.                                                                   | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 22 | The top `N` populated capital cities in a region where `N` is provided by the user.                                                                      | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 23 | The population of people, people living in cities, and people not living in cities in each continent.                                                    | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 24 | The population of people, people living in cities, and people not living in cities in each region.                                                       | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 25 | The population of people, people living in cities, and people not living in cities in each country.                                                      | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 26 | The population of the world.                                                                                                                             | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 27 | The population of a continent.                                                                                                                           | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 28 | The population of a region.                                                                                                                              | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 29 | The population of a country.                                                                                                                             | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 30 | The population of a district.                                                                                                                            | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 31 | The population of a city.                                                                                                                                | Yes | ![Screenshot of terminal](./doc/demo/requirements/1.1.png)                                                                |
| 32 | The number of people who speak Chinese, English, Hindi, Spanish, and Arabic, from greatest to smallest, including the percentage of the world population | No  |                                                                                                                           |

## Coverage

Each square is a file, color-coded to how much of it is being tested.

![Grid map of code coverage](https://codecov.io/gh/2004seraph/SET08103/graphs/tree.svg?token=EDH24ELB68)

## Dev Notes

- The `.env` file stores some repeated variables for the build process for the docker containers
- The `./build.sh` file simply builds the Maven project, then builds the docker containers
  - You can run the file from your command line by typing `./build.sh`
- The `./start.sh` file starts the entire project, it works similarly to above, and will build the project if it needs
to, but if you edit it, you will need to rerun `./build.sh`
- To completely reset all the docker containers, run: `docker compose rm`, and select "Yes"
- Changing `MYSQL_ROOT_PASSWORD` requires `docker compose rm` to be ran