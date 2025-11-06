package com.example.queryexecutor.service;

import com.example.queryexecutor.exception.QueryExecutionException;
import com.example.queryexecutor.model.AsyncQueryStatus;
import com.example.queryexecutor.model.StoredQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryExecutionServiceTest {

    @Mock
    private QueryService queryService;

    private QueryExecutionService queryExecutionService;

    @BeforeEach
    void setUp() {
        // Create a real instance without mocking DataSource
        // This allows validation logic to run
        queryExecutionService = new QueryExecutionService(null, queryService);
    }

    @Test
    void executeQuery_InsertQuery_ShouldThrowException() {
        // Given
        StoredQuery insertQuery = new StoredQuery();
        insertQuery.setId(2L);
        insertQuery.setQuery("INSERT INTO passengers VALUES (1, 'Test')");
        when(queryService.getQueryById(2L)).thenReturn(insertQuery);

        // When & Then
        QueryExecutionException exception = assertThrows(
                QueryExecutionException.class,
                () -> queryExecutionService.executeQuery(2L)
        );
        assertTrue(exception.getMessage().contains("Only SELECT queries are allowed"));
    }

    @Test
    void executeQuery_UpdateQuery_ShouldThrowException() {
        // Given
        StoredQuery updateQuery = new StoredQuery();
        updateQuery.setId(3L);
        updateQuery.setQuery("UPDATE passengers SET Name = 'Test' WHERE PassengerId = 1");
        when(queryService.getQueryById(3L)).thenReturn(updateQuery);

        // When & Then
        QueryExecutionException exception = assertThrows(
                QueryExecutionException.class,
                () -> queryExecutionService.executeQuery(3L)
        );
        assertTrue(exception.getMessage().contains("forbidden keyword") ||
                exception.getMessage().contains("Only SELECT queries are allowed"));
    }

    @Test
    void executeQuery_DeleteQuery_ShouldThrowException() {
        // Given
        StoredQuery deleteQuery = new StoredQuery();
        deleteQuery.setId(4L);
        deleteQuery.setQuery("DELETE FROM passengers WHERE PassengerId = 1");
        when(queryService.getQueryById(4L)).thenReturn(deleteQuery);

        // When & Then
        QueryExecutionException exception = assertThrows(
                QueryExecutionException.class,
                () -> queryExecutionService.executeQuery(4L)
        );
        assertTrue(exception.getMessage().contains("forbidden keyword") ||
                exception.getMessage().contains("Only SELECT queries are allowed"));
    }

    @Test
    void executeQuery_DropQuery_ShouldThrowException() {
        // Given
        StoredQuery dropQuery = new StoredQuery();
        dropQuery.setId(5L);
        dropQuery.setQuery("DROP TABLE passengers");
        when(queryService.getQueryById(5L)).thenReturn(dropQuery);

        // When & Then
        QueryExecutionException exception = assertThrows(
                QueryExecutionException.class,
                () -> queryExecutionService.executeQuery(5L)
        );
        assertTrue(exception.getMessage().contains("forbidden keyword") ||
                exception.getMessage().contains("Only SELECT queries are allowed"));
    }

    @Test
    void executeQueryAsync_ShouldReturnExecutionId() {
        // Given
        StoredQuery validSelectQuery = new StoredQuery();
        validSelectQuery.setId(1L);
        validSelectQuery.setQuery("SELECT * FROM passengers");
        when(queryService.getQueryById(eq(1L))).thenReturn(validSelectQuery);

        // When
        String executionId = queryExecutionService.executeQueryAsync(1L);

        // Then
        assertNotNull(executionId);
        assertFalse(executionId.isEmpty());

        // Verify we can get the status
        AsyncQueryStatus status = queryExecutionService.getAsyncQueryStatus(executionId);
        assertNotNull(status);
        assertEquals(executionId, status.getExecutionId());
    }

    @Test
    void getAsyncQueryStatus_InvalidExecutionId_ShouldThrowException() {
        // When & Then
        assertThrows(
                com.example.queryexecutor.exception.AsyncExecutionNotFoundException.class,
                () -> queryExecutionService.getAsyncQueryStatus("invalid-id")
        );
    }
}