package com.example.queryexecutor.service;

import com.example.queryexecutor.exception.QueryExecutionException;
import com.example.queryexecutor.model.AsyncQueryStatus;
import com.example.queryexecutor.model.StoredQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryExecutionService {

    private final DataSource dataSource;
    private final QueryService queryService;
    
    // Store async execution results
    private final Map<String, AsyncQueryStatus> asyncResults = new ConcurrentHashMap<>();

    /**
     * Execute a stored query and return results.
     * Results are cached to improve performance for repeated queries.
     *
     * @param queryId The ID of the stored query
     * @return 2D list of query results
     */
    @Cacheable(value = "queryResults", key = "#queryId")
    public List<List<Object>> executeQuery(Long queryId) {
        StoredQuery storedQuery = queryService.getQueryById(queryId);
        String sql = storedQuery.getQuery();
        
        log.info("Executing query ID {}: {}", queryId, sql);
        
        // Validate query is read-only
        validateReadOnlyQuery(sql);
        
        return executeSelectQuery(sql);
    }

    /**
     * Execute a query asynchronously.
     * Returns an execution ID that can be used to check status.
     *
     * @param queryId The ID of the stored query
     * @return Execution ID
     */
    public String executeQueryAsync(Long queryId) {
        String executionId = UUID.randomUUID().toString();
        asyncResults.put(executionId, AsyncQueryStatus.pending(executionId));
        
        // Start async execution
        executeQueryAsyncInternal(queryId, executionId);
        
        return executionId;
    }

    /**
     * Get the status of an async query execution.
     *
     * @param executionId The execution ID
     * @return Query status
     */
    public AsyncQueryStatus getAsyncQueryStatus(String executionId) {
        AsyncQueryStatus status = asyncResults.get(executionId);
        if (status == null) {
            throw new com.example.queryexecutor.exception.AsyncExecutionNotFoundException(executionId);
        }
        return status;
    }

    @Async
    protected void executeQueryAsyncInternal(Long queryId, String executionId) {
        try {
            // Update status to running
            asyncResults.put(executionId, AsyncQueryStatus.running(executionId));
            
            // Execute the query
            List<List<Object>> result = executeQuery(queryId);
            
            // Update status to completed
            asyncResults.put(executionId, AsyncQueryStatus.completed(executionId, result));
            
            log.info("Async query execution {} completed successfully", executionId);
            
        } catch (Exception e) {
            log.error("Async query execution {} failed", executionId, e);
            asyncResults.put(executionId, AsyncQueryStatus.failed(executionId, e.getMessage()));
        }
    }

    /**
     * Validate that the query is read-only (SELECT only).
     * This prevents data modification through the API.
     *
     * @param sql The SQL query to validate
     * @throws QueryExecutionException if query is not read-only
     */
    private void validateReadOnlyQuery(String sql) {
        String trimmedSql = sql.trim().toUpperCase();
        
        // Check if query starts with SELECT
        if (!trimmedSql.startsWith("SELECT")) {
            throw new QueryExecutionException("Only SELECT queries are allowed. Query must be read-only.");
        }
        
        // Check for dangerous keywords that might modify data
        String[] dangerousKeywords = {"INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE", "REPLACE", "MERGE"};
        for (String keyword : dangerousKeywords) {
            if (trimmedSql.contains(keyword)) {
                throw new QueryExecutionException("Query contains forbidden keyword: " + keyword + ". Only SELECT queries are allowed.");
            }
        }
        
        log.debug("Query validated as read-only");
    }

    /**
     * Execute a SELECT query using a read-only connection.
     *
     * @param sql The SQL query to execute
     * @return 2D list of results
     */
    private List<List<Object>> executeSelectQuery(String sql) {
        List<List<Object>> results = new ArrayList<>();
        
        // Use a separate read-only connection for extra safety
        try (Connection conn = getReadOnlyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Process result set
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                results.add(row);
            }
            
            log.info("Query executed successfully, returned {} rows", results.size());
            
        } catch (SQLException e) {
            log.error("Error executing query: {}", sql, e);
            throw new QueryExecutionException("Failed to execute query: " + e.getMessage(), e);
        }
        
        return results;
    }

    /**
     * Get a read-only database connection.
     * This provides an additional layer of protection against data modification.
     *
     * @return Read-only connection
     * @throws SQLException if connection cannot be obtained
     */
    private Connection getReadOnlyConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setReadOnly(true);
        conn.setAutoCommit(false);
        return conn;
    }
}
