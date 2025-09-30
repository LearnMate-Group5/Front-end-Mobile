package com.example.learnmate_frontend.model;

public class GenericResponse {
    private boolean success;
    private String message;

    public GenericResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
