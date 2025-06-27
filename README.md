# Lottery System - Bynder challenge
Code challenge of a Lottery System for Bynder hiring process

## How to use (Commands and requirements)

### Requirements
This is a Spring Boot application with dependency management done via Gradle (build.gradle.kts).

Java 17 is required.

### Spring Boot commands
**To build the app (and also run tests):** 

`./gradlew clean build`

**To run the server:** 

`./gradlew bootRun`

The server will be started in port 8080 (http://localhost:8080/)

### System functionalities

**To register a user:**

It can be done via cURL (POST) in  http://localhost:8080/api/participant/register

An example of cURL:

`curl --json '{"name": "Maria", "email": "maria@example.com"}' http://localhost:8080/api/participant/register
`

_Remember that you can only register a participant if the email has not been registered before_