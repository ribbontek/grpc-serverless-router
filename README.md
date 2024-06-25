# gRPC Serverless Router Example

A gRPC Serverless Router Demo application using SpringBoot 3.2, Kotlin 1.9, & Java 21.

This project demonstrates an experimental approach with routing through a REST endpoint & passing through to a gRPC service. 

## Java 21

Installing Java 21 on Linux/Ubuntu

- `sudo apt-get install openjdk-21-jdk`

## Build the Project

Building the whole project:

```shell
./gradlew clean build -i
```

## Format the Project with KtLint

Formatting the whole project:

```shell
./gradlew ktlintFormat -i
```

## Running the whole stack locally

The docker-compose file in the main directory runs the grpc service & database. To run the serverless functions, open up the readme there for more information

Run the docker-compose file:

`docker-compose up -d`

## Version the Project with Axion

Tagging a new release (ensure to have appropriate permissions in GitHub)

```shell
./gradlew release -Prelease.disableChecks
```

Use the `-Prelease.scope` flag to indicate incremental level, i.e. `-Prelease.scope=incrementMajor` for a major release

Test it out locally with `-Prelease.dryRun`

Versioning starts from `1.0.0` by default
