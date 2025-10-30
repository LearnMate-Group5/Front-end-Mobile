package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model cho Chat Session
 */
public class ChatSessionResponse {
    @SerializedName("sessionId")
    public String sessionId;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("lastMessageAt")
    public String lastMessageAt;

    @SerializedName("messageCount")
    public int messageCount;

    @SerializedName("messages")
    public List<ChatMessage> messages;

    public static class ChatMessage {
        @SerializedName("messageId")
        public String messageId;

        @SerializedName("role")
        public String role; // "user", "assistant"

        @SerializedName("content")
        public String content;

        @SerializedName("timestamp")
        public String timestamp;
    }
}
