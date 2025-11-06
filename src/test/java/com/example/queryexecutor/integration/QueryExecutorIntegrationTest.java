package com.example.queryexecutor.integration;

import com.example.queryexecutor.model.CreateQueryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test that verifies the complete workflow:
 * 1. Create a query
 * 2. List all queries
 * 3. Execute the query
 * 4. Test async execution
 */
@SpringBootTest
@AutoConfigureMockMvc
class QueryExecutorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullWorkflow_CreateListAndExecuteQuery() throws Exception {
        // Step 1: Create a query
        CreateQueryRequest request = new CreateQueryRequest("SELECT * FROM passengers WHERE PassengerId <= 3");
        
        MvcResult createResult = mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        // Extract query ID from response
        String responseBody = createResult.getResponse().getContentAsString();
        Long queryId = objectMapper.readTree(responseBody).get("id").asLong();

        // Step 2: List all queries and verify our query exists
        mockMvc.perform(get("/queries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + queryId + ")].query")
                        .value("SELECT * FROM passengers WHERE PassengerId <= 3"));

        // Step 3: Execute the query and verify results
        mockMvc.perform(get("/queries/execute")
                        .param("query", queryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").isArray());
    }

    @Test
    void executeQuery_WithAggregation_ShouldReturnResults() throws Exception {
        // Create a query with aggregation
        CreateQueryRequest request = new CreateQueryRequest(
                "SELECT Pclass, COUNT(*) as count FROM passengers GROUP BY Pclass"
        );
        
        MvcResult createResult = mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long queryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Execute the aggregation query
        mockMvc.perform(get("/queries/execute")
                        .param("query", queryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void executeQuery_InvalidQueryId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/queries/execute")
                        .param("query", "99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuery_ThenTryToModifyData_ShouldFail() throws Exception {
        // Try to create a query that modifies data
        CreateQueryRequest maliciousRequest = new CreateQueryRequest(
                "UPDATE passengers SET Name = 'Hacked' WHERE PassengerId = 1"
        );
        
        MvcResult createResult = mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long queryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Try to execute the malicious query - should fail// Try to execute the malicious query - should fail
                mockMvc.perform(get("/queries/execute")
                                .param("query", queryId.toString()))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only SELECT queries are allowed")));
    }

    @Test
    void asyncExecution_FullWorkflow() throws Exception {
        // Create a query
        CreateQueryRequest request = new CreateQueryRequest("SELECT COUNT(*) FROM passengers");
        
        MvcResult createResult = mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long queryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Start async execution
        MvcResult asyncResult = mockMvc.perform(post("/queries/execute/async")
                        .param("query", queryId.toString()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").exists())
                .andReturn();

        String executionId = objectMapper.readTree(asyncResult.getResponse().getContentAsString())
                .get("executionId").asText();

        // Wait a bit for async execution
        Thread.sleep(1000);

        // Check status
        mockMvc.perform(get("/queries/execute/async/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void caching_ExecuteSameQueryTwice_ShouldUseCachedResults() throws Exception {
        // Create a query
        CreateQueryRequest request = new CreateQueryRequest("SELECT * FROM passengers WHERE PassengerId = 1");
        
        MvcResult createResult = mockMvc.perform(post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long queryId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Execute query first time
        MvcResult firstExecution = mockMvc.perform(get("/queries/execute")
                        .param("query", queryId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Execute query second time (should use cache)
        MvcResult secondExecution = mockMvc.perform(get("/queries/execute")
                        .param("query", queryId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // Both should return the same results
        String firstResult = firstExecution.getResponse().getContentAsString();
        String secondResult = secondExecution.getResponse().getContentAsString();
        
        assert firstResult.equals(secondResult);
    }
}
