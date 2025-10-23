package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

public class AiChatResponse {
    @SerializedName("output")
    private String output;
    
    public AiChatResponse() {}
    
    public AiChatResponse(String output) {
        this.output = output;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
}
