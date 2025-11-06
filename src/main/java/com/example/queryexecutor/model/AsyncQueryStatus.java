package com.example.queryexecutor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncQueryStatus {
    
    public enum Status {
        PENDING, RUNNING, COMPLETED, FAILED
    }
    
    private String executionId;
    private Status status;
    private List<List<Object>> result;
    private String errorMessage;
    
    public static AsyncQueryStatus pending(String executionId) {
        AsyncQueryStatus status = new AsyncQueryStatus();
        status.setExecutionId(executionId);
        status.setStatus(Status.PENDING);
        return status;
    }
    
    public static AsyncQueryStatus running(String executionId) {
        AsyncQueryStatus status = new AsyncQueryStatus();
        status.setExecutionId(executionId);
        status.setStatus(Status.RUNNING);
        return status;
    }
    
    public static AsyncQueryStatus completed(String executionId, List<List<Object>> result) {
        AsyncQueryStatus status = new AsyncQueryStatus();
        status.setExecutionId(executionId);
        status.setStatus(Status.COMPLETED);
        status.setResult(result);
        return status;
    }
    
    public static AsyncQueryStatus failed(String executionId, String errorMessage) {
        AsyncQueryStatus status = new AsyncQueryStatus();
        status.setExecutionId(executionId);
        status.setStatus(Status.FAILED);
        status.setErrorMessage(errorMessage);
        return status;
    }
}
