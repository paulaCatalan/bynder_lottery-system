# Lottery System - Bynder challenge
Code challenge of a Lottery System for Bynder hiring process

## How to use (Commands and requirements)

### Requirements
This is a Spring Boot application with dependency management done via Gradle (build.gradle.kts).

Java 17 is required.

### Database Setup (MongoDB with Docker)

This application uses MongoDB for data persistence in production. A Docker Compose setup is provided for easy local development.

#### Prerequisites
- Docker and Docker Compose installed on your machine

#### MongoDB Setup Commands

**1. Start MongoDB database:**
```bash
docker-compose up -d mongodb
```

**2. Stop MongoDB database:**
```bash
docker-compose down
```

**3. View MongoDB logs:**
```bash
docker logs lottery-mongodb
```

**4. Connect to MongoDB shell (for debugging):**
```bash
docker exec -it lottery-mongodb mongosh mongodb://lottery_user:lottery_password@localhost:27017/lottery_system_db
```

#### Database Configuration

The application is configured to use MongoDB in production mode (default profile) with the following settings:
- **Database**: `lottery_system_db`
- **Host**: `localhost:27017`
- **Username**: `lottery_user`
- **Password**: `lottery_password`

The `init-mongo.js` script automatically:
- Creates the database and user
- Sets up unique indexes on `id` and `email` fields for the participants collection

#### Data Persistence

- **Participant Data**: Always persisted to MongoDB database (in production mode)
- **Lottery Events & Ballots**: Stored in JSON files for both production and test modes
- **Test Mode**: All data (including participants) uses JSON files for testing isolation

The application uses a hybrid persistence approach:
- MongoDB provides robust, scalable storage for participant registration
- JSON files maintain simplicity for lottery events and ballot management

**🏗️ Architecture Highlight**: This project demonstrates how clean architecture with dependency inversion makes persistence technology switches seamless. The transition from JSON to MongoDB for participant data required zero changes to business logic - only swapping repository implementations. The same pattern could easily be applied to migrate lottery events and ballots to MongoDB or any other database.

### Spring Boot commands

**Prerequisites: Start MongoDB first:**
```bash
docker-compose up -d mongodb
```

**To build the app (and also run tests):**

`./gradlew clean build`

**To run the server:**

`./gradlew bootRun`

The server will be started in port 8080 (http://localhost:8080/)

**To stop everything:**
```bash
# Stop the Spring Boot application (Ctrl+C in the terminal where it's running)
# Stop MongoDB
docker-compose down
```

#### Troubleshooting

**MongoDB connection issues:**
- Verify MongoDB is running: `docker ps` (should show `lottery-mongodb` container)
- Check MongoDB logs: `docker logs lottery-mongodb`
- Restart MongoDB: `docker-compose restart mongodb`

**Clean slate setup:**
```bash
# Stop everything
docker-compose down

# Remove MongoDB data volume (⚠️ This will delete all data!)
docker volume rm bynder_lottery-system-use-ddbb-for-persistance_mongo-data

# Start fresh
docker-compose up -d mongodb
./gradlew bootRun
```

**View database contents:**
```bash
# List all participants
docker exec lottery-mongodb mongosh mongodb://lottery_user:lottery_password@localhost:27017/lottery_system_db --eval "db.participants.find().pretty()"

# Count participants
docker exec lottery-mongodb mongosh mongodb://lottery_user:lottery_password@localhost:27017/lottery_system_db --eval "db.participants.countDocuments()"
```

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