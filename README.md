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

## System functionalities

### User registration

It can be done via cURL (POST) in  http://localhost:8080/api/participant/register

An example of cURL:

`curl --json '{"name": "Maria", "email": "maria@example.com"}' http://localhost:8080/api/participant/register
`

_Remember that you can only register a participant if the email has not been registered before_

### Retrieve OPEN lotteries (that accept participation)

It can be done via GET in this endpoint: http://localhost:8080/api/lotteries/open

### Retrieve DRAWN lotteries by date (end date of lottery)
It can be done via GET in this endpoint: http://localhost:8080/api/lotteries/drawn/{yyyy-mm-dd}

## Other resources related to this development

### Diagrams and organization
#### MIRO Board
The development of this project is backed up by visual diagrams and tasks organization
[in this MIRO Board](https://miro.com/app/board/uXjVIkTX1yU=/?share_link_id=960275875808)

### Version Control and Commits

This project's development adheres to best practices in version control, focusing on:

* **Atomic Commits:** Changes are introduced in small, focused steps, with each commit addressing a single logical change.
* **Continuous Integration Readiness:** Every commit ensures the project remains in a buildable and functional state.
* **Clear Commit Messages:** Commit messages are concise, descriptive, and enhanced with a relevant Gitmoji from [this emoji dictionary](https://gitmoji.dev/) for quick visual context.