package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model cho chi tiết Chat Session từ /api/Ai/session/{sessionId}
 */
public class ChatSessionDetailResponse {
    @SerializedName("sessionId")
    public String sessionId;

    @SerializedName("userId")
    public String userId;

    @SerializedName("messages")
    public List<SessionMessage> messages;

    /**
     * Message trong session detail
     * message field là JSON string cần parse thêm
     */
    public static class SessionMessage {
        @SerializedName("id")
        public int id;

        @SerializedName("sessionId")
        public String sessionId;

        @SerializedName("message")
        public String message; // JSON string: {"type": "human/ai", "content": "..."}
    }

    /**
     * Parsed message content từ JSON string
     */
    public static class ParsedMessage {
        @SerializedName("type")
        public String type; // "human" hoặc "ai"

        @SerializedName("content")
        public String content;
    }
}

