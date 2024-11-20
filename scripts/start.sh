#!/bin/bash
# Which shell interpreter to use, important for all shell scripts, can be ignored

# Imagine the file mentioned below is simply copy-pasted to this position
. ./scripts/envars.sh

# Runs the docker image as a container
docker compose up --abort-on-container-exit "$@"