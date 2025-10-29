package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.AiChatRequest;
import com.example.LearnMate.network.dto.AiChatResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiChatService {
    @POST("n8n/webhook/ai-chat")
    Call<List<AiChatResponse>> sendMessage(@Body AiChatRequest request);
}
