package com.example.queryexecutor.service;

import com.example.queryexecutor.exception.QueryNotFoundException;
import com.example.queryexecutor.model.CreateQueryRequest;
import com.example.queryexecutor.model.CreateQueryResponse;
import com.example.queryexecutor.model.QueryDTO;
import com.example.queryexecutor.model.StoredQuery;
import com.example.queryexecutor.repository.StoredQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    @Mock
    private StoredQueryRepository queryRepository;

    @InjectMocks
    private QueryService queryService;

    private StoredQuery sampleQuery;

    @BeforeEach
    void setUp() {
        sampleQuery = new StoredQuery();
        sampleQuery.setId(1L);
        sampleQuery.setQuery("SELECT * FROM passengers");
    }

    @Test
    void createQuery_ShouldSaveAndReturnId() {
        // Given
        CreateQueryRequest request = new CreateQueryRequest("SELECT * FROM passengers");
        when(queryRepository.save(any(StoredQuery.class))).thenReturn(sampleQuery);

        // When
        CreateQueryResponse response = queryService.createQuery(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(queryRepository, times(1)).save(any(StoredQuery.class));
    }

    @Test
    void getAllQueries_ShouldReturnAllQueries() {
        // Given
        StoredQuery query2 = new StoredQuery();
        query2.setId(2L);
        query2.setQuery("SELECT COUNT(*) FROM passengers");
        
        when(queryRepository.findAll()).thenReturn(Arrays.asList(sampleQuery, query2));

        // When
        List<QueryDTO> queries = queryService.getAllQueries();

        // Then
        assertNotNull(queries);
        assertEquals(2, queries.size());
        assertEquals(1L, queries.get(0).getId());
        assertEquals("SELECT * FROM passengers", queries.get(0).getQuery());
        verify(queryRepository, times(1)).findAll();
    }

    @Test
    void getQueryById_WhenQueryExists_ShouldReturnQuery() {
        // Given
        when(queryRepository.findById(1L)).thenReturn(Optional.of(sampleQuery));

        // When
        StoredQuery result = queryService.getQueryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SELECT * FROM passengers", result.getQuery());
        verify(queryRepository, times(1)).findById(1L);
    }

    @Test
    void getQueryById_WhenQueryNotExists_ShouldThrowException() {
        // Given
        when(queryRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(QueryNotFoundException.class, () -> queryService.getQueryById(99L));
        verify(queryRepository, times(1)).findById(99L);
    }
}
