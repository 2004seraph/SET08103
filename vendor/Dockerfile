# Use the MySQL image version 8.3 (allows native password plugin)
FROM mysql:8.3
# Set the working directory
WORKDIR /tmp

# Copy all the files to the working directory of the container
# Copy the main SQL file to docker-entrypoint-initdb.d.
COPY world.sql /docker-entrypoint-initdb.d
# Set the root password
ENV MYSQL_ROOT_PASSWORD example