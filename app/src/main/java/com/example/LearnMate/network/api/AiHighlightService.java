package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.AiHighlightRequest;
import com.example.LearnMate.network.dto.AiHighlightResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiHighlightService {
    @POST("n8n/webhook/ai-highlight")
    Call<List<AiHighlightResponse>> getHighlightInfo(@Body AiHighlightRequest request);
}

