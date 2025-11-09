package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for Text-to-Speech API
 */
public class TTSRequest {
    
    @SerializedName("text")
    private String text;
    
    @SerializedName("speed")
    private float speed;
    
    @SerializedName("voiceid")
    private String voiceId;
    
    @SerializedName("userid")
    private String userId;
    
    @SerializedName("uniqueid")
    private String uniqueId;

    public TTSRequest(String text, float speed, String voiceId, String userId, String uniqueId) {
        this.text = text;
        this.speed = speed;
        this.voiceId = voiceId;
        this.userId = userId;
        this.uniqueId = uniqueId;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
