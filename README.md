# SET08103 - Software Engineering Methods, Team 3 Project Repository

## Dev Notes

- The `.env` file stores some repeated variables for the build process for the docker containers
- The `./build.sh` file simply builds the Maven project, then builds the docker containers
  - You can run the file from your command line by typing `./build.sh`
- The `./start.sh` file starts the entire project, it works similarly to above, and will build the project if it needs
to, but if you edit it, you will need to rerun `./build.sh`
- To completely reset all the docker containers, run: `docker compose rm`, and select "Yes"
- Changing `MYSQL_ROOT_PASSWORD` requires `docker compose rm` to be ran