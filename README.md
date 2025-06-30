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

_💡Pre-populated json files have been added so the functionality is easier to test on clone._

### User registration

It can be done via cURL (POST) in  http://localhost:8080/api/participant/register

An example of cURL:

`
curl --json '{"name": "Maria", "email": "maria@example.com"}' http://localhost:8080/api/participant/register
`

_Remember that you can only register a participant if the email has not been registered before_

### Retrieving OPEN lotteries (that accept participation)

It can be done via GET in this endpoint: http://localhost:8080/api/lotteries/open

### Retrieving DRAWN lotteries by date (end date of lottery)
It can be done via GET in this endpoint: http://localhost:8080/api/lotteries/drawn/{yyyy-mm-dd}

### Participating in a lottery
The lottery has to be open and the participant needs to be registered.

It can be done via a cURL:

`
curl --json '{"lotteryId": "11111111-1111-1111-1111-111111111111", "participantId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"}' http://localhost:8080/api/lotteries/participate
`

### Lottery Opening and Closing (Automatic actions)

The Lottery System automates the lifecycle of daily lottery events through scheduled tasks:

* **Lottery Opening:**

    * A scheduled task runs daily at **9:00 AM (09:00:00)**.

    * Its primary function is to create a new `OPEN` lottery event for the current day.


* **Lottery Closing and Winner Drawing:**

    * A separate scheduled task runs daily at **midnight (00:00:00)**.

    * This task identifies *all* currently `OPEN` lottery events.

    * It then proceeds to change the status of each of these `OPEN` lotteries to `CLOSED`.

    * Immediately after closing, it attempts to **draw a winner** for each closed lottery.

    * If there are submitted ballots, a random winner is selected, and the lottery status is updated to `DRAWN`.

    * If there are no ballots submitted for a closed lottery, it remains in the `CLOSED` status with no winner.

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