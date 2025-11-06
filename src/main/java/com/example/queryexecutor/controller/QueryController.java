package com.example.queryexecutor.controller;

import com.example.queryexecutor.model.*;
import com.example.queryexecutor.service.QueryExecutionService;
import com.example.queryexecutor.service.QueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/queries")
@Slf4j
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;
    private final QueryExecutionService queryExecutionService;


    @PostMapping
    public ResponseEntity<CreateQueryResponse> createQuery(@Valid @RequestBody CreateQueryRequest request) {
        log.info("POST /queries - Creating new query");
        CreateQueryResponse response = queryService.createQuery(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<QueryDTO>> getAllQueries() {
        log.info("GET /queries - Fetching all queries");
        List<QueryDTO> queries = queryService.getAllQueries();
        return ResponseEntity.ok(queries);
    }


    @GetMapping("/execute")
    public ResponseEntity<List<List<Object>>> executeQuery(@RequestParam("query") Long queryId) {
        log.info("GET /queries/execute?query={} - Executing query", queryId);
        List<List<Object>> results = queryExecutionService.executeQuery(queryId);
        return ResponseEntity.ok(results);
    }


    @PostMapping("/execute/async")
    public ResponseEntity<Map<String, String>> executeQueryAsync(@RequestParam("query") Long queryId) {
        log.info("POST /queries/execute/async?query={} - Starting async execution", queryId);
        String executionId = queryExecutionService.executeQueryAsync(queryId);
        
        Map<String, String> response = new HashMap<>();
        response.put("executionId", executionId);
        response.put("statusUrl", "/queries/execute/async/" + executionId);
        
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }


    @GetMapping("/execute/async/{executionId}")
    public ResponseEntity<AsyncQueryStatus> getAsyncQueryStatus(@PathVariable String executionId) {
        log.info("GET /queries/execute/async/{} - Fetching async query status", executionId);
        AsyncQueryStatus status = queryExecutionService.getAsyncQueryStatus(executionId);
        return ResponseEntity.ok(status);
    }
}
