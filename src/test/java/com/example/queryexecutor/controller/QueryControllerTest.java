package com.example.queryexecutor.controller;

import com.example.queryexecutor.model.*;
import com.example.queryexecutor.service.QueryExecutionService;
import com.example.queryexecutor.service.QueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QueryService queryService;

    @MockBean
    private QueryExecutionService queryExecutionService;

    @Test
    void createQuery_ValidRequest_ShouldReturn201() throws Exception {
        // Given
        CreateQueryRequest request = new CreateQueryRequest("SELECT * FROM passengers");
        CreateQueryResponse response = new CreateQueryResponse(1L);
        when(queryService.createQuery(any(CreateQueryRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createQuery_EmptyQuery_ShouldReturn400() throws Exception {
        // Given
        CreateQueryRequest request = new CreateQueryRequest("");

        // When & Then
        mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllQueries_ShouldReturnQueryList() throws Exception {
        // Given
        List<QueryDTO> queries = Arrays.asList(
                new QueryDTO(1L, "SELECT * FROM passengers"),
                new QueryDTO(2L, "SELECT COUNT(*) FROM passengers")
        );
        when(queryService.getAllQueries()).thenReturn(queries);

        // When & Then
        mockMvc.perform(get("/queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].query").value("SELECT * FROM passengers"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void executeQuery_ValidQueryId_ShouldReturnResults() throws Exception {
        // Given
        List<List<Object>> results = Arrays.asList(
                Arrays.asList(1, "John", 25),
                Arrays.asList(2, "Jane", 30)
        );
        when(queryExecutionService.executeQuery(eq(1L))).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/queries/execute")
                        .param("query", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0][0]").value(1))
                .andExpect(jsonPath("$[0][1]").value("John"))
                .andExpect(jsonPath("$[1][0]").value(2));
    }

    @Test
    void executeQueryAsync_ShouldReturnExecutionId() throws Exception {
        // Given
        String executionId = "test-execution-id";
        when(queryExecutionService.executeQueryAsync(eq(1L))).thenReturn(executionId);

        // When & Then
        mockMvc.perform(post("/queries/execute/async")
                        .param("query", "1"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(executionId))
                .andExpect(jsonPath("$.statusUrl").exists());
    }

    @Test
    void getAsyncQueryStatus_ValidExecutionId_ShouldReturnStatus() throws Exception {
        // Given
        String executionId = "test-execution-id";
        AsyncQueryStatus status = AsyncQueryStatus.pending(executionId);
        when(queryExecutionService.getAsyncQueryStatus(eq(executionId))).thenReturn(status);

        // When & Then
        mockMvc.perform(get("/queries/execute/async/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
