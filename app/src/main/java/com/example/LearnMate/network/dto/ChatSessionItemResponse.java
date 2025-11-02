package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho một Chat Session item trong danh sách
 * Dùng cho /api/Ai/session - trả về array trực tiếp
 */
public class ChatSessionItemResponse {
    @SerializedName("sessionId")
    public String sessionId;

    @SerializedName("userId")
    public String userId;

    @SerializedName("title")
    public String title;

    @SerializedName("createdDate")
    public String createdDate;

    @SerializedName("lastActivityDate")
    public String lastActivityDate;

    @SerializedName("messageCount")
    public int messageCount;
}

