# SET08103 - Software Engineering Methods

## Team 3 Project Repository

![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/2004seraph/SET08103/docker-image.yml?branch=master)
[![LICENSE](https://img.shields.io/github/license/2004seraph/SET08103.svg?style=flat-square)](https://github.com/2004seraph/SET08103/blob/master/LICENSE)
[![Releases](https://img.shields.io/github/release/2004seraph/SET08103/all.svg?style=flat-square)](https://github.com/2004seraph/SET08103/releases)
[![codecov](https://codecov.io/gh/2004seraph/SET08103/branch/master/graph/badge.svg?token=EDH24ELB68)](https://codecov.io/gh/2004seraph/SET08103)

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