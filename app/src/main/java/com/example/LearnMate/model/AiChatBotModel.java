package com.example.LearnMate.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AiChatService;
import com.example.LearnMate.network.api.AiService;
import com.example.LearnMate.network.dto.AiChatRequest;
import com.example.LearnMate.network.dto.AiChatResponse;
import com.example.LearnMate.network.dto.AiFileResponse;
import com.example.LearnMate.network.dto.ChatSessionDetailResponse;
import com.example.LearnMate.network.dto.ChatSessionItemResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatBotModel {
    private static final String TAG = "AiChatBotModel";
    
    private final AiChatService aiChatService;
    private final AiService aiService;

    public AiChatBotModel(Context context) {
        this.aiChatService = RetrofitClient.getAiChatService(context);
        this.aiService = RetrofitClient.getRetrofitWithAuth(context).create(AiService.class);
    }

    /**
     * Check if user has files
     */
    public void checkFiles(@NonNull Context context, @NonNull FilesCheckCallback callback) {
        // Check token first
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("user_token", null);
        if (token == null || token.isEmpty()) {
            token = prefs.getString("token", null);
        }
        
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "No token found in SharedPreferences! Showing locked state.");
            callback.onNoFiles();
            return;
        }
        
        aiService.getFiles().enqueue(new Callback<List<AiFileResponse>>() {
            @Override
            public void onResponse(Call<List<AiFileResponse>> call, Response<List<AiFileResponse>> response) {
                boolean hasFiles = false;
                
                if (response.isSuccessful()) {
                    try {
                        ResponseBody responseBody = response.raw().body();
                        String bodyString = null;
                        if (responseBody != null) {
                            bodyString = responseBody.string();
                            Log.d(TAG, "API Response (200): " + bodyString);
                        } else if (response.body() != null) {
                            Gson gson = new Gson();
                            bodyString = gson.toJson(response.body());
                            Log.d(TAG, "API Response (from body): " + bodyString);
                        }
                        
                        if (bodyString != null && !bodyString.isEmpty()) {
                            Gson gson = new Gson();
                            JsonParser parser = new JsonParser();
                            JsonElement jsonElement = parser.parse(bodyString);
                            
                            List<AiFileResponse> files = new ArrayList<>();
                            
                            if (jsonElement.isJsonArray()) {
                                JsonArray jsonArray = jsonElement.getAsJsonArray();
                                for (JsonElement element : jsonArray) {
                                    AiFileResponse file = gson.fromJson(element, AiFileResponse.class);
                                    if (file != null && file.fileId != null) {
                                        files.add(file);
                                    }
                                }
                                hasFiles = !files.isEmpty();
                            } else if (jsonElement.isJsonObject()) {
                                JsonObject jsonObject = jsonElement.getAsJsonObject();
                                if (jsonObject.has("files") && jsonObject.get("files").isJsonArray()) {
                                    JsonArray filesArray = jsonObject.get("files").getAsJsonArray();
                                    for (JsonElement element : filesArray) {
                                        AiFileResponse file = gson.fromJson(element, AiFileResponse.class);
                                        if (file != null && file.fileId != null) {
                                            files.add(file);
                                        }
                                    }
                                    hasFiles = !files.isEmpty();
                                } else if (jsonObject.has("fileId")) {
                                    AiFileResponse file = gson.fromJson(jsonObject, AiFileResponse.class);
                                    if (file != null && file.fileId != null) {
                                        hasFiles = true;
                                    }
                                }
                            }
                            
                            if (!hasFiles && response.body() != null && !response.body().isEmpty()) {
                                hasFiles = true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response body: " + e.getMessage());
                        if (response.body() != null && !response.body().isEmpty()) {
                            hasFiles = true;
                        }
                    }
                } else {
                    int statusCode = response.code();
                    if (statusCode == 503) {
                        Log.w(TAG, "503 Service Temporarily Unavailable");
                        RetrofitClient.clearCache();
                        callback.onRetryNeeded();
                        return;
                    }
                }
                
                if (hasFiles) {
                    callback.onHasFiles();
                } else {
                    callback.onNoFiles();
                }
            }

            @Override
            public void onFailure(Call<List<AiFileResponse>> call, Throwable t) {
                Log.e(TAG, "Error checking files: " + t.getMessage());
                callback.onNoFiles();
            }
        });
    }

    /**
     * Load chat sessions
     */
    public void loadChatSessions(@NonNull SessionsCallback callback) {
        aiService.getSessions().enqueue(new Callback<List<ChatSessionItemResponse>>() {
            @Override
            public void onResponse(Call<List<ChatSessionItemResponse>> call, Response<List<ChatSessionItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load sessions: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ChatSessionItemResponse>> call, Throwable t) {
                callback.onError("Error loading sessions: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * Load session messages
     */
    public void loadSessionMessages(@NonNull String sessionId, @NonNull SessionMessagesCallback callback) {
        aiService.getSession(sessionId).enqueue(new Callback<ChatSessionDetailResponse>() {
            @Override
            public void onResponse(Call<ChatSessionDetailResponse> call, Response<ChatSessionDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load session messages: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatSessionDetailResponse> call, Throwable t) {
                callback.onError("Error loading session messages: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * Send chat message
     */
    public void sendMessage(@NonNull String message, @NonNull String sessionId, @NonNull String userId, @NonNull SendMessageCallback callback) {
        AiChatRequest request = new AiChatRequest(message, sessionId, userId);
        aiChatService.sendMessage(request).enqueue(new Callback<List<AiChatResponse>>() {
            @Override
            public void onResponse(Call<List<AiChatResponse>> call, Response<List<AiChatResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String botResponse = response.body().get(0).getOutput();
                    if (botResponse != null && !botResponse.isEmpty()) {
                        callback.onSuccess(botResponse);
                    } else {
                        callback.onError("Empty response from AI");
                    }
                } else {
                    callback.onError("Failed to get response from AI (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<AiChatResponse>> call, Throwable t) {
                String errorMessage;
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Request timeout. AI is taking longer than expected to respond.";
                } else {
                    errorMessage = "Network error: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
                }
                callback.onError(errorMessage);
            }
        });
    }

    public interface FilesCheckCallback {
        void onHasFiles();
        void onNoFiles();
        void onRetryNeeded();
    }

    public interface SessionsCallback {
        void onSuccess(List<ChatSessionItemResponse> sessions);
        void onError(String message);
    }

    public interface SessionMessagesCallback {
        void onSuccess(ChatSessionDetailResponse session);
        void onError(String message);
    }

    public interface SendMessageCallback {
        void onSuccess(String botResponse);
        void onError(String message);
    }
}

