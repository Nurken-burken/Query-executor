# Query Executor Service - Design Document

## Overview

A Spring Boot REST API for storing and executing analytical SQL queries over the Titanic passenger dataset. Built with Spring Boot 3.2.0, H2 in-memory database, and tested with JUnit 5/Mockito.

## API Endpoints

### POST /queries
Store a SQL query for later execution.

**Request:**
```json
{
  "query": "SELECT * FROM passengers WHERE Age > 30"
}
```

**Response:**
```json
{
  "id": 1
}
```

### GET /queries
List all stored queries.

### GET /queries/execute?query={id}
Execute query synchronously, returns 2D array of results.

### POST /queries/execute/async?query={id}
Start async execution, returns execution ID immediately (202 status).

### GET /queries/execute/async/{executionId}
Check execution status and retrieve results when complete.

## Read-Only Query Enforcement

Three layers of protection to prevent data modification:

1. **Query Validation** - Checks that queries start with SELECT and blocks keywords like INSERT, UPDATE, DELETE, DROP, etc. Fast but could potentially be bypassed with creative SQL.

2. **Read-Only Connection** - Sets `connection.setReadOnly(true)` at the JDBC level. The database driver enforces this, which catches anything that gets past layer 1.

3. **Database Permissions** - Not implemented since H2 doesn't require auth, but in production you'd use a dedicated read-only database user.

The first two layers are enough for this use case. Layer 1 gives clear error messages, layer 2 is the real enforcement.

## Async Execution

Long queries can take forever, so there's an async option. When you POST to `/execute/async`, you get an execution ID immediately (202 status). The query runs in a background thread pool (configured for 5-10 threads). You poll the status endpoint to check progress.

Status is tracked in a `ConcurrentHashMap` in memory, which means results disappear on restart. Not ideal for production but fine for a demo. In a real system you'd want to persist this to a database or use a proper message queue like RabbitMQ.

## Caching

Uses Spring's `@Cacheable` to cache query results by ID. Since the Titanic dataset never changes, this makes sense - why re-run the same query? Simple `ConcurrentHashMap` implementation, cleared on restart.

One gotcha: cache key is the query ID, not the query text itself. So if you somehow reuse IDs, you'd get stale results. For this project it's fine since there's no delete endpoint.

## What I Assumed

- Dataset is static (read-only)

- Small enough to fit in memory (~900 rows)

- Users are trusted and know SQL

- Single instance deployment

- Short-lived (results don't need to survive restarts)

- Low concurrency

## Known Limitations

**Security:**
- No authentication or rate limiting - anyone can run queries
- Keyword validation blocks obvious SQL injection but doesn't parse the actual SQL AST
- Users can still run expensive queries that DOS the system

**Scalability:**
- Everything's in memory - large result sets will cause OOM errors
- Fixed thread pool size with no query timeouts

**Reliability:**
- Basic error handling but no retry logic, metrics, or health checks

This is development-grade security. Good for internal tools with trusted users, not for anything public-facing.

## How to Break It

- **Memory exhaustion** - Run a cartesian join that returns millions of rows

- **Thread pool saturation** - Submit 100 slow queries at once

- **Async map overflow** - Keep requesting async executions until the HashMap explodes

- **Cache poisoning** - Reuse query IDs (if delete was implemented)

## Production Improvements

For real production use, you'd need:

- Authentication (Spring Security + JWT)
- Persistent storage (PostgreSQL instead of H2)
- Proper async task queue (RabbitMQ)
- Distributed caching (Redis)
- Query timeouts and result limits
- Metrics and monitoring (Micrometer/Prometheus)
- Query deletion endpoint
- Row-level security if multiple users have different data access levels
- WebSockets for async status updates instead of polling

## Testing

Unit tests cover services and controllers using JUnit/Mockito. Integration tests with `@SpringBootTest` verify the full workflow: create query, list, execute, async execution, caching. All the main paths are covered plus error cases.

---

**Nurken Samigullin, 2025**
