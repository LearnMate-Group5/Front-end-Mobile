package com.example.LearnMate.network.dto;

import java.util.List;

/**
 * Response DTO for List of Books
 */
public class BookListResponse {
    private List<BookResponse> value;
    private Boolean isSuccess;
    private String message;
    private List<String> errors;
    
    // Getters and Setters
    public List<BookResponse> getValue() {
        return value;
    }
    
    public void setValue(List<BookResponse> value) {
        this.value = value;
    }
    
    public Boolean getIsSuccess() {
        return isSuccess;
    }
    
    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}

