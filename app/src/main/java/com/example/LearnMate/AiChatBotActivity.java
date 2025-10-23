package com.example.LearnMate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.R;
import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.model.ChatMessage;
import com.example.LearnMate.network.api.AiChatService;
import com.example.LearnMate.network.dto.AiChatRequest;
import com.example.LearnMate.network.dto.AiChatResponse;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.adapter.ChatAdapter;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatBotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private CircularProgressIndicator progressIndicator;
    private BottomNavigationComponent bottomNavComponent;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private AiChatService aiChatService;
    private String userId;
    private String sessionId = "1234sdfs741"; // Default session ID as requested

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chatbot);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        initializeServices();
        loadUserId();
        
        // Add welcome message
        addWelcomeMessage();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        progressIndicator = findViewById(R.id.progressIndicator);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNavComponent.setSelectedItem(R.id.nav_ai_bot);
    }

    private void initializeServices() {
        aiChatService = RetrofitClient.getAiChatService(this);
    }

    private void loadUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please login again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
            "Hello! I'm your AI assistant. How can I help you today?",
            ChatMessage.TYPE_BOT,
            System.currentTimeMillis()
        );
        chatMessages.add(welcomeMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        // Add user message to chat
        ChatMessage userMessage = new ChatMessage(
            message,
            ChatMessage.TYPE_USER,
            System.currentTimeMillis()
        );
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Clear input
        editTextMessage.setText("");

        // Show loading
        showLoading(true);

        // Send to API
        AiChatRequest request = new AiChatRequest(message, sessionId, userId);
        Log.d("AiChatBot", "Sending request: " + request.getMessage() + " | SessionId: " + request.getSessionId() + " | UserId: " + request.getUserId());
        
        aiChatService.sendMessage(request).enqueue(new Callback<List<AiChatResponse>>() {
            @Override
            public void onResponse(Call<List<AiChatResponse>> call, Response<List<AiChatResponse>> response) {
                showLoading(false);
                Log.d("AiChatBot", "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String botResponse = response.body().get(0).getOutput();
                    Log.d("AiChatBot", "Bot response: " + botResponse);
                    
                    if (botResponse != null && !botResponse.isEmpty()) {
                        ChatMessage botMessage = new ChatMessage(
                            botResponse,
                            ChatMessage.TYPE_BOT,
                            System.currentTimeMillis()
                        );
                        chatMessages.add(botMessage);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                    } else {
                        Log.e("AiChatBot", "Empty response from AI");
                        showError("Empty response from AI");
                    }
                } else {
                    Log.e("AiChatBot", "Response not successful: " + response.code() + " - " + response.message());
                    showError("Failed to get response from AI (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<AiChatResponse>> call, Throwable t) {
                showLoading(false);
                Log.e("AiChatBot", "Network error", t);
                
                String errorMessage;
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Request timeout. AI is taking longer than expected to respond. Please try again.";
                } else {
                    errorMessage = "Network error: " + t.getMessage();
                }
                
                showError(errorMessage);
            }
        });
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSend.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Add error message to chat
        String chatErrorMessage;
        if (message.contains("timeout")) {
            chatErrorMessage = "⏰ I'm taking longer than expected to process your request. This might be a complex question that needs more time to think about. Please try again!";
        } else {
            chatErrorMessage = "Sorry, I encountered an error. Please check your network connection and try again.";
        }
        
        ChatMessage errorMessage = new ChatMessage(
            chatErrorMessage,
            ChatMessage.TYPE_BOT,
            System.currentTimeMillis()
        );
        chatMessages.add(errorMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }
}
