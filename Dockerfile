FROM openjdk:latest

ARG PROJECT_ARTIFACT_ID
ARG PROJECT_VERSION
ARG MYSQL_ROOT_PASSWORD

ENV PROJECT_ARTIFACT_ID=${PROJECT_ARTIFACT_ID}
ENV PROJECT_VERSION=${PROJECT_VERSION}
ENV MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}

WORKDIR /tmp

COPY ./target/${PROJECT_ARTIFACT_ID}-${PROJECT_VERSION}-jar-with-dependencies.jar /tmp
ENTRYPOINT java -jar ${PROJECT_ARTIFACT_ID}-${PROJECT_VERSION}-jar-with-dependencies.jar