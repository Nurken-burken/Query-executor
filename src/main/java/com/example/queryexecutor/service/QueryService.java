package com.example.queryexecutor.service;

import com.example.queryexecutor.exception.QueryNotFoundException;
import com.example.queryexecutor.model.CreateQueryRequest;
import com.example.queryexecutor.model.CreateQueryResponse;
import com.example.queryexecutor.model.QueryDTO;
import com.example.queryexecutor.model.StoredQuery;
import com.example.queryexecutor.repository.StoredQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

    private final StoredQueryRepository queryRepository;

    @Transactional
    public CreateQueryResponse createQuery(CreateQueryRequest request) {
        log.info("Creating new query: {}", request.getQuery());
        
        StoredQuery storedQuery = new StoredQuery();
        storedQuery.setQuery(request.getQuery());
        
        StoredQuery saved = queryRepository.save(storedQuery);
        log.info("Query saved with ID: {}", saved.getId());
        
        return new CreateQueryResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<QueryDTO> getAllQueries() {
        log.info("Fetching all stored queries");
        return queryRepository.findAll().stream()
                .map(q -> new QueryDTO(q.getId(), q.getQuery()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoredQuery getQueryById(Long id) {
        log.info("Fetching query with ID: {}", id);
        return queryRepository.findById(id)
                .orElseThrow(() -> new QueryNotFoundException(id));
    }
}
