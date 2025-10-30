package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response cho danh sách chat sessions
 */
public class ChatSessionListResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("sessions")
    public List<ChatSessionResponse> sessions;

    @SerializedName("totalCount")
    public int totalCount;
}
