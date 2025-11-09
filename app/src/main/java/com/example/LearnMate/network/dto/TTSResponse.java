package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for Text-to-Speech API
 */
public class TTSResponse {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("result")
    private String result;  // Audio URL
    
    @SerializedName("voice_id")
    private String voiceId;
    
    @SerializedName("model_id")
    private String modelId;
    
    @SerializedName("error")
    private String error;
    
    @SerializedName("speed")
    private float speed;
    
    @SerializedName("pitch")
    private float pitch;
    
    @SerializedName("volume")
    private float volume;
    
    @SerializedName("language")
    private String language;
    
    @SerializedName("is_clone")
    private boolean isClone;
    
    @SerializedName("process_percentage")
    private int processPercentage;

    // Getters
    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public String getModelId() {
        return modelId;
    }

    public String getError() {
        return error;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPitch() {
        return pitch;
    }

    public float getVolume() {
        return volume;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isClone() {
        return isClone;
    }

    public int getProcessPercentage() {
        return processPercentage;
    }
    
    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }
}
