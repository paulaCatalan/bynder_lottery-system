# ADR 001: Use JSON Files for Persistence

## Status
Accepted

## Context
Initial development of the Lottery System. 
Focus on quick prototyping, easy setup, and understanding data flow without complex external dependencies. 
Exploring clean architecture and DDD. However, it has been considered that an important part of the functionality
of this challenge is that data must persist across application restarts, ruling out purely in-memory solutions like HashMaps.

## Decision
Use **JSON files** for persistence of `Participant` and `LotteryEvent` data.

## Consequences

### Positive
* **Simple Setup:** No external database needed, fast to get started.
* **Readable Data:** Easy to inspect and debug data directly in files.
* **Fast Prototyping:** Quick iterations on domain logic without database overhead.
* **Easy DTO Mapping:** Straightforward conversion between JSON and Java objects.
* **Clear Persistence Layer:** Reinforces Hexagonal Architecture boundaries.

### Negative
* **Limited Scalability:** Poor performance for large datasets; entire file often read/rewritten.
* **Concurrency Issues:** `ReentrantReadWriteLock` is a bottleneck; less robust than database concurrency.
* **No Transactions/Integrity:** Lacks ACID properties; data consistency is manual.
* **Schema Changes:** Evolving data structure can be cumbersome.
* **Infrastructure Overhead:** Required manual implementation of file handling logic (e.g., `JsonFileHandler`), adding complexity to the infrastructure layer that a database would abstract.

## Alternatives Considered

* **H2 Database (In-Memory/File):** Full SQL, transactions, better queries. More setup than JSON.
* **Relational Database (e.g., PostgreSQL):** Robust, scalable, ACID, powerful querying. Requires external setup and ORM.
* **NoSQL Database (e.g., MongoDB):** Flexible schema, high scalability. Requires external setup.

## Conclusion

JSON files are chosen for **simplicity, rapid development, and ease of data inspection** in this early phase. We acknowledge its limitations in scalability, concurrency, and integrity.

**Future Consideration:** A migration to a more robust persistence solution (like H2 or a full database) will be a planned future step as the project grows. The repository pattern will ease this transition.
