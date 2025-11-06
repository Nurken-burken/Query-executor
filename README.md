# Query Executor Service

A Spring Boot REST API for storing and executing analytical SQL queries over the Titanic passengers dataset.

## 🎯 Features

- **Query Storage**: Store SQL queries for later execution
- **Query Listing**: View all stored queries with their IDs
- **Synchronous Execution**: Execute queries and get immediate results
- **Asynchronous Execution**: Execute long-running queries in the background
- **Read-Only Enforcement**: Guarantees queries cannot modify data
- **Performance Caching**: Cached results for repeated queries
- **Comprehensive Tests**: Unit tests and integration tests included

## 📋 Prerequisites

- Java 17 or higher
- Gradle 8.5 or higher (or use included Gradle wrapper)
- IntelliJ IDEA (recommended) or any Java IDE
- Postman (for API testing)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd query-executor
```

### 2. Build the Project

Using Gradle wrapper (recommended):
```bash
./gradlew clean build
```

Or using your local Gradle installation:
```bash
gradle clean build
```

### 3. Run the Application

Using Gradle:
```bash
./gradlew bootRun
```

Or run the JAR directly:
```bash
java -jar build/libs/query-executor-1.0.0.jar
```

The application will start on `http://localhost:8080`

### 4. Verify It's Running

Open your browser or use curl:
```bash
curl http://localhost:8080/queries
```

You should see an empty array `[]` (no queries stored yet).

## 🧪 Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests QueryServiceTest
```

### View Test Report

After running tests, open:
```
build/reports/tests/test/index.html
```

## 📚 API Documentation

### Base URL

```
http://localhost:8080
```

### Endpoints

#### 1. Create Query

Store a new SQL query for later execution.

```bash
POST /queries
Content-Type: application/json

{
  "query": "SELECT * FROM passengers WHERE Age > 30"
}
```

**Response (201 Created)**:
```json
{
  "id": 1
}
```

#### 2. List All Queries

Get all stored queries with their IDs.

```bash
GET /queries
```

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "query": "SELECT * FROM passengers WHERE Age > 30"
  },
  {
    "id": 2,
    "query": "SELECT Pclass, COUNT(*) FROM passengers GROUP BY Pclass"
  }
]
```

#### 3. Execute Query (Sync)

Execute a stored query and return results immediately.

```bash
GET /queries/execute?query=1
```

**Response (200 OK)**:
```json
[
  [1, 0, 3, "Braund, Mr. Owen Harris", "male", 22, ...],
  [2, 1, 1, "Cumings, Mrs. John Bradley", "female", 38, ...]
]
```

#### 4. Execute Query (Async)

Start query execution in the background.

```bash
POST /queries/execute/async?query=1
```

**Response (202 Accepted)**:
```json
{
  "executionId": "550e8400-e29b-41d4-a716-446655440000",
  "statusUrl": "/queries/execute/async/550e8400-e29b-41d4-a716-446655440000"
}
```

#### 5. Check Async Status

Check the status of an asynchronous query execution.

```bash
GET /queries/execute/async/{executionId}
```

**Response (200 OK)**:
```json
{
  "executionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "result": [[1, 2, 3], [4, 5, 6]],
  "errorMessage": null
}
```

**Status values**: `PENDING`, `RUNNING`, `COMPLETED`, `FAILED`

## 💡 Example Queries to Try

### Basic Queries

```sql
-- Get all passengers
SELECT * FROM passengers;

-- Get passengers who survived
SELECT * FROM passengers WHERE Survived = 1;

-- Count passengers by class
SELECT Pclass, COUNT(*) as count FROM passengers GROUP BY Pclass;

-- Average age by gender
SELECT Sex, AVG(Age) as avg_age FROM passengers GROUP BY Sex;

-- Passengers older than 30
SELECT * FROM passengers WHERE Age > 30;
```

### Aggregation Queries

```sql
-- Survival rate by class
SELECT Pclass, 
       SUM(Survived) as survived, 
       COUNT(*) as total,
       CAST(SUM(Survived) AS FLOAT) / COUNT(*) as survival_rate
FROM passengers 
GROUP BY Pclass;

-- Most expensive tickets
SELECT Name, Pclass, Fare FROM passengers 
ORDER BY Fare DESC 
LIMIT 10;

-- Family size analysis
SELECT SibSp + Parch as family_size, 
       COUNT(*) as count, 
       SUM(Survived) as survived
FROM passengers 
GROUP BY SibSp + Parch
ORDER BY family_size;
```

## 🧪 Testing with Postman

### 1. Import Collection (if provided)

If a Postman collection is included:
1. Open Postman
2. Click Import
3. Select `postman_collection.json`
4. All endpoints will be ready to test

### 2. Manual Testing

1. **Create a Query**:
   - Method: POST
   - URL: `http://localhost:8080/queries`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "query": "SELECT * FROM passengers WHERE PassengerId <= 5"
     }
     ```
   - Note the returned `id`

2. **List Queries**:
   - Method: GET
   - URL: `http://localhost:8080/queries`
   - Verify your query appears in the list

3. **Execute Query**:
   - Method: GET
   - URL: `http://localhost:8080/queries/execute?query=1`
   - See the results as a 2D array

4. **Try Async Execution**:
   - Method: POST
   - URL: `http://localhost:8080/queries/execute/async?query=1`
   - Note the `executionId`

5. **Check Async Status**:
   - Method: GET
   - URL: `http://localhost:8080/queries/execute/async/{executionId}`
   - Replace `{executionId}` with the ID from step 4

## 🗄️ Database

The application uses H2 in-memory database with:
- **passengers** table: Titanic dataset (5 sample records for quick testing)
- **stored_queries** table: Your saved queries

### H2 Console

Access the H2 web console for debugging:

1. Navigate to: `http://localhost:8080/h2-console`
2. Use these settings:
   - **JDBC URL**: `jdbc:h2:mem:querydb`
   - **Username**: `sa`
   - **Password**: (leave empty)
3. Click "Connect"

You can run queries directly here to test your SQL before storing it via the API.

## 🏗️ Project Structure

```
query-executor/
├── src/
│   ├── main/
│   │   ├── java/com/example/queryexecutor/
│   │   │   ├── config/           # Database and async configuration
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── exception/        # Custom exceptions and handlers
│   │   │   ├── model/            # Domain models and DTOs
│   │   │   ├── repository/       # JPA repositories
│   │   │   ├── service/          # Business logic
│   │   │   └── QueryExecutorApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── titanic.csv       # Sample dataset
│   └── test/
│       └── java/com/example/queryexecutor/
│           ├── controller/       # Controller tests
│           ├── service/          # Service tests
│           └── integration/      # Integration tests
├── build.gradle                  # Dependencies and build config
├── DESIGN.md                     # Comprehensive design document
└── README.md                     # This file
```

## 🔒 Security Notes

**⚠️ This is a development/demo application. Not production-ready!**

- No authentication/authorization
- No rate limiting
- No input sanitization beyond read-only validation
- No query timeouts
- No result size limits

See `DESIGN.md` for detailed security considerations and production recommendations.

## 📖 Documentation

For detailed information about:
- Architecture decisions
- Read-only enforcement strategy
- Async execution implementation
- Caching strategy
- Limitations and known issues
- Future improvements

**Please read**: [DESIGN.md](DESIGN.md)

## 🐛 Troubleshooting

### Port 8080 already in use

Change the port in `src/main/resources/application.properties`:
```properties
server.port=8081
```

### Tests failing

Make sure you have Java 17:
```bash
java -version
```

Clean and rebuild:
```bash
./gradlew clean build
```

### H2 console not accessible

Verify in `application.properties`:
```properties
spring.h2.console.enabled=true
```

### Cannot execute queries

Check the logs for specific error messages:
```bash
./gradlew bootRun
```

Look for exceptions in the console output.

## 🤝 Contributing

This is an assignment project, but suggestions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📝 License

This project is for educational purposes.

## 👤 Author

[Your Name]

## 📞 Contact

For questions about this project, please contact [your email] or open an issue.

---

**Happy Querying! 🚀**
