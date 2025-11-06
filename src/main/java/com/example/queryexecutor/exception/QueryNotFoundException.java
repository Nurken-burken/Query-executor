package com.example.queryexecutor.exception;

public class QueryNotFoundException extends RuntimeException {
    public QueryNotFoundException(Long id) {
        super("Query not found with id: " + id);
    }
}
