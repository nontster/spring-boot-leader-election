# Project Overview

This is a Spring Boot project for leader election. It contains a simple REST controller that returns a message indicating the current leader. The project is configured to use Java 17 and Maven for dependency management.

# Building and Running

## Building the project

To build the project, run the following command:

```bash
./mvnw clean install
```

## Running the project

To run the project, use the following command:

```bash
./mvnw spring-boot:run
```

Once the application is running, you can access the leader endpoint at `http://localhost:8080/leader`.

## Running tests

To run the tests, use the following command:

```bash
./mvnw test
```

# Development Conventions

The project follows the standard Spring Boot conventions. The main application class is `com.example.leader.demo.DemoApplication`. The application properties are located in `src/main/resources/application.properties`. The REST controllers are located in the `com.example.leader.demo` package.
