package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.TTSRequest;
import com.example.LearnMate.network.dto.TTSResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Service interface for Text-to-Speech API
 */
public interface TTSService {
    
    /**
     * Convert text to speech
     * POST https://d2j7q4aa2pvgbz.cloudfront.net/n8n/webhook/max-text-to-speech
     * 
     * @param request TTS request with text, speed, voiceId, userId, uniqueId
     * @return List of TTS responses with audio URL
     */
    @POST("n8n/webhook/max-text-to-speech")
    Call<List<TTSResponse>> convertTextToSpeech(@Body TTSRequest request);
}
