package com.example.queryexecutor.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQueryRequest {
    
    @NotBlank(message = "Query text cannot be blank")
    private String query;
}
