package com.example.queryexecutor.exception;

public class AsyncExecutionNotFoundException extends RuntimeException {
    public AsyncExecutionNotFoundException(String executionId) {
        super("Async execution not found with id: " + executionId);
    }
}
