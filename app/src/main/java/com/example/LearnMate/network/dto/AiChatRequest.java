package com.example.LearnMate.network.dto;

public class AiChatRequest {
    private String message;
    private String sessionId;
    private String userId;
    
    public AiChatRequest(String message, String sessionId, String userId) {
        this.message = message;
        this.sessionId = sessionId;
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
