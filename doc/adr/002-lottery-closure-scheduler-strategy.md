# ADR 002: Lottery Closure Scheduler Strategy

## Status

Accepted

## Context

The Lottery System requires a daily lottery event to start at 9:00 AM and close at midnight. The current project uses JSON files for persistence, which has significant file I/O implications. The goal is a functional and efficient solution for this fixed-time closure.

## Decision

We will use a **single `@Scheduled` task configured to run precisely at midnight (00:00:00)** each day. This task will find and close all currently `OPEN` lottery events.

## Consequences

### Positive

* **Simplicity:** Straightforward cron expression for a fixed-time closure.

* **Efficiency for Current Needs:** Runs once daily, minimizing file I/O for JSON persistence.

* **Clear Intent:** Explicitly defines the daily lottery closure time.

* **Reduced Overhead:** Avoids constant polling and processing overhead.

### Negative

* **Limited Flexibility:** Tightly coupled to a fixed midnight closure; cannot easily handle arbitrary end times.

* **Future Refactoring:** Requires significant refactoring (and likely a database migration) if dynamic closure times are needed later.

## Alternatives Considered

* **Frequent Scanning (e.g., every minute):**

    * **Pros:** Highly flexible, capable of closing lotteries at any `endTime`.

    * **Cons:** **Impractical with JSON files.** Would involve reading/rewriting the entire `lottery-events.json` file every minute, causing severe performance bottlenecks and resource consumption. This approach is only viable with a proper database optimized for efficient querying.

* **Event-Driven / Time-Based Queue Approach (Advanced Future Consideration):**
    This is a more robust and scalable solution for systems needing precise, dynamic, and high-volume time-based event processing, typically used in larger, distributed systems.

    * **How it Works:**

        * When a `LotteryEvent` is created or opened, a message (e.g., "close lottery X") is **scheduled for future delivery** to a dedicated messaging system or job scheduler. The message is set to be delivered precisely at the lottery's `endTime`.

        * A separate service or consumer constantly monitors this time-based queue. When a scheduled message's time arrives, it's consumed.

        * The consuming service then fetches the specific `LotteryEvent` by ID from the database, closes it, and proceeds with winner selection.

    * **Advantages:**

        * **Precision:** Events trigger very close to their exact scheduled time.

        * **Scalability:** Designed to handle millions of scheduled events efficiently without constant polling.

        * **Decoupling:** Lottery creation is decoupled from its closure process.

        * **No Polling Overhead:** The system only activates when an event is actually due.

    * **Disadvantages:**

        * **Increased Complexity:** Introduces new infrastructure (message brokers, dedicated schedulers like Quartz, AWS SQS, Google Cloud Tasks) and more moving parts.

        * **Overkill for Simple Cases:** Definitely unnecessary for a single daily lottery with a fixed closure time.

        * **Requires a Proper Database:** This approach is only efficient and scalable when backed by a robust database that can quickly retrieve individual lottery events.

## Conclusion

The choice of a single `@Scheduled` task at midnight is a pragmatic and efficient solution tailored to the current project's requirements and technical constraints (JSON file persistence). It prioritizes simplicity and performance for the fixed daily lottery closure.

**Future Consideration:** Should the system's needs evolve to include lotteries with dynamic or arbitrary closure times, a re-evaluation of the persistence layer (migrating to a database) and a more advanced, potentially event-driven or frequent-scanning scheduling strategy will be necessary. The current decision allows for rapid development and verification of core functionality without premature optimization or over-engineering.