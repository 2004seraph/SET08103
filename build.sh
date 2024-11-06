#!/bin/bash
# Which shell interpreter to use, important for all shell scripts, can be ignored

# Imagine the file mentioned below is simply copy-pasted to this position
. ./scripts/envars.sh

# Builds the Java project into a self-contained JAR file
mvn package -DskipTests
# Sets up the run environment (docker container) and copies the JAR file there
docker compose build