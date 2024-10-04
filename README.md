# SET08103 - Software Engineering Methods, Team 3 Project Repository

## Project Structure

This project follows the same format seen in the previous labs, but with some small 
additions that basically amount to typing less stuff into the console and allows more 
focus on Java coding.

We use a feature of Docker called Docker Compose to automate some simple things. Mainly 
passing project meta information such as the `version` defined in the maven `pom.xml` 
file over to the docker image, so it does not need to manually updated each version change.

- This is done inside the `build.sh` script. It simply runs each line as a terminal 
- command in sequence.

Other than that, the `docker-compose.yml` file isn't used for anything else.

### Getting up and running

To build the docker container, run `./build.sh` in the repository folder. If this doesn't 
work, you may need to run `chmod +x ./build.sh` before trying again.

To then run the docker container, run `./start.sh`. You may need to do the same fix as 
above but with this file as well.

