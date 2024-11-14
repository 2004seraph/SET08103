#!/bin/bash
# Which shell interpreter to use, important for all shell scripts, can be ignored

# Loads the .env file variables
while IFS== read -r key value; do
  printf -v "$key" %s "$value" && export "$key"
done <.env

# Grabs some info about the project (file name and version) from the pom.xml file
# this means the version is only specified there and updates automatically in all the docker configuration.
export PROJECT_ARTIFACT_ID="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.artifactId}' --non-recursive exec:exec)"
export PROJECT_VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"