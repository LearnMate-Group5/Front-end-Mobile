package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.TranslateRequest;
import com.example.LearnMate.network.dto.TranslateResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiTranslateService {
    @POST("n8n:5678/webhook/upload-and-translate")
    Call<TranslateResponse> translate(@Body TranslateRequest body);
}
