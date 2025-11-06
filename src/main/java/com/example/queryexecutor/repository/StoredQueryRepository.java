package com.example.queryexecutor.repository;

import com.example.queryexecutor.model.StoredQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredQueryRepository extends JpaRepository<StoredQuery, Long> {
}
