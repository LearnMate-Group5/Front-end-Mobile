package com.example.LearnMate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.R;
import com.example.LearnMate.ImportActivity;
import com.example.LearnMate.adapter.ChatAdapter;
import com.example.LearnMate.adapter.ChatSessionAdapter;
import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.model.ChatMessage;
import com.example.LearnMate.network.api.AiChatService;
import com.example.LearnMate.network.api.AiService;
import com.example.LearnMate.network.dto.AiChatRequest;
import com.example.LearnMate.network.dto.AiChatResponse;
import com.example.LearnMate.network.dto.AiFileListResponse;
import com.example.LearnMate.network.dto.AiFileResponse;
import com.example.LearnMate.network.dto.ChatSessionItemResponse;
import com.example.LearnMate.network.dto.ChatSessionDetailResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.ResponseBody;
import com.example.LearnMate.network.RetrofitClient;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatBotActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonMenu;
    private CircularProgressIndicator progressIndicator;
    private BottomNavigationComponent bottomNavComponent;

    // Sidebar views
    private RecyclerView recyclerViewChatSessions;
    private Button buttonNewChat;
    private ImageButton buttonCloseSidebar;
    private TextView textEmptyChatHistory;

    // Locked state views
    private View mainContentLayout;
    private View lockedStateLayout;
    private BottomNavigationComponent lockedBottomNavComponent;
    private Button buttonImportFile;

    private ChatAdapter chatAdapter;
    private ChatSessionAdapter sessionAdapter;
    private List<ChatMessage> chatMessages;
    private List<ChatSessionItemResponse> chatSessions;
    private AiChatService aiChatService;
    private AiService aiService;
    private String userId;
    private String sessionId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chatbot);

        initViews();
        initializeServices();
        loadUserId();

        // Check if user has files before setting up chat
        checkFilesAndSetupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check files when returning from Import Activity
        // This ensures UI updates if user imported files
        if (lockedStateLayout != null && lockedStateLayout.getVisibility() == View.VISIBLE) {
            checkFilesAndSetupUI();
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        mainContentLayout = findViewById(R.id.mainContentLayout);
        lockedStateLayout = findViewById(R.id.lockedStateLayout);

        recyclerView = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonMenu = findViewById(R.id.buttonMenu);
        progressIndicator = findViewById(R.id.progressIndicator);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Sidebar views
        recyclerViewChatSessions = findViewById(R.id.recyclerViewChatSessions);
        buttonNewChat = findViewById(R.id.buttonNewChat);
        buttonCloseSidebar = findViewById(R.id.buttonCloseSidebar);
        textEmptyChatHistory = findViewById(R.id.textEmptyChatHistory);

        // Locked state views
        lockedBottomNavComponent = findViewById(R.id.lockedBottomNavComponent);
        buttonImportFile = findViewById(R.id.buttonImportFile);
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupSidebarRecyclerView() {
        chatSessions = new ArrayList<>();
        sessionAdapter = new ChatSessionAdapter();
        sessionAdapter.updateSessions(chatSessions);
        recyclerViewChatSessions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChatSessions.setAdapter(sessionAdapter);

        sessionAdapter.setOnSessionClickListener(session -> {
            loadSessionMessages(session.sessionId);
            closeDrawer();
        });
    }

    private void setupClickListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        buttonMenu.setOnClickListener(v -> openDrawer());
        buttonNewChat.setOnClickListener(v -> {
            startNewChat();
            closeDrawer();
        });
        buttonCloseSidebar.setOnClickListener(v -> closeDrawer());

        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(findViewById(R.id.sidebarLayout));
        }
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(findViewById(R.id.sidebarLayout));
        }
    }

    private void startNewChat() {
        sessionId = "";
        chatMessages.clear();
        chatAdapter.notifyDataSetChanged();

        // Clear selection in sidebar
        if (sessionAdapter != null) {
            sessionAdapter.setSelectedSessionId(null);
        }

        addWelcomeMessage();
    }

    private void setupBottomNavigation() {
        bottomNavComponent.setSelectedItem(R.id.nav_ai_bot);
    }

    private void initializeServices() {
        aiChatService = RetrofitClient.getAiChatService(this);
        aiService = RetrofitClient.getRetrofitWithAuth(this).create(AiService.class);
    }

    /**
     * Check if user has imported files and setup UI accordingly
     * API có thể trả về:
     * 1. Array trực tiếp: [{...}, {...}]
     * 2. Single object: {...}
     * 3. Wrapper object: {success: true, files: [...], ...}
     */
    private void checkFilesAndSetupUI() {
        // Check token before making request
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("user_token", null);
        if (token == null || token.isEmpty()) {
            token = prefs.getString("token", null);
        }

        if (token == null || token.isEmpty()) {
            Log.w("AiChatBot", "No token found in SharedPreferences! Showing locked state.");
            showLockedState();
            return;
        } else {
            Log.d("AiChatBot", "Token found. Length: " + token.length() + " characters");
        }

        // Always create fresh service instance to ensure latest token is used
        aiService = RetrofitClient.getRetrofitWithAuth(this).create(AiService.class);

        aiService.getFiles().enqueue(new Callback<List<AiFileResponse>>() {
            @Override
            public void onResponse(Call<List<AiFileResponse>> call, Response<List<AiFileResponse>> response) {
                boolean hasFiles = false;

                // Check response status
                if (response.isSuccessful()) {
                    // Success: parse response body
                    try {
                        // Try to get response body as string
                        ResponseBody responseBody = response.raw().body();
                        String bodyString = null;
                        if (responseBody != null) {
                            bodyString = responseBody.string();
                            Log.d("AiChatBot", "API Response (200): " + bodyString);
                        } else if (response.body() != null) {
                            // Fallback: try to get from response.body()
                            // Since response.body() expects List, we need to parse manually
                            Gson gson = new Gson();
                            bodyString = gson.toJson(response.body());
                            Log.d("AiChatBot", "API Response (from body): " + bodyString);
                        }

                        if (bodyString != null && !bodyString.isEmpty()) {

                            Gson gson = new Gson();
                            JsonParser parser = new JsonParser();
                            JsonElement jsonElement = parser.parse(bodyString);

                            List<AiFileResponse> files = new ArrayList<>();

                            if (jsonElement.isJsonArray()) {
                                // Case 1: Array trực tiếp [{...}, {...}]
                                JsonArray jsonArray = jsonElement.getAsJsonArray();
                                for (JsonElement element : jsonArray) {
                                    AiFileResponse file = gson.fromJson(element, AiFileResponse.class);
                                    if (file != null && file.fileId != null) {
                                        files.add(file);
                                    }
                                }
                                hasFiles = !files.isEmpty();
                                Log.d("AiChatBot", "Parsed as array. Found " + files.size() + " files.");
                            } else if (jsonElement.isJsonObject()) {
                                JsonObject jsonObject = jsonElement.getAsJsonObject();

                                // Check if it's wrapper object {success, files[], ...}
                                if (jsonObject.has("files") && jsonObject.get("files").isJsonArray()) {
                                    // Case 3: Wrapper object
                                    JsonArray filesArray = jsonObject.get("files").getAsJsonArray();
                                    for (JsonElement element : filesArray) {
                                        AiFileResponse file = gson.fromJson(element, AiFileResponse.class);
                                        if (file != null && file.fileId != null) {
                                            files.add(file);
                                        }
                                    }
                                    hasFiles = !files.isEmpty();
                                    Log.d("AiChatBot", "Parsed as wrapper object. Found " + files.size() + " files.");
                                } else if (jsonObject.has("fileId")) {
                                    // Case 2: Single object {...}
                                    AiFileResponse file = gson.fromJson(jsonObject, AiFileResponse.class);
                                    if (file != null && file.fileId != null) {
                                        files.add(file);
                                        hasFiles = true;
                                    }
                                    Log.d("AiChatBot", "Parsed as single object. Found 1 file.");
                                }
                            }

                            // Check result from response.body() as fallback
                            if (!hasFiles && response.body() != null && !response.body().isEmpty()) {
                                List<AiFileResponse> filesList = response.body();
                                hasFiles = !filesList.isEmpty();
                                Log.d("AiChatBot", "Using response.body(). Found " + filesList.size() + " files.");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("AiChatBot", "Error parsing response body: " + e.getMessage());
                        e.printStackTrace();

                        // Fallback: try response.body()
                        if (response.body() != null && !response.body().isEmpty()) {
                            hasFiles = true;
                            Log.d("AiChatBot", "Fallback: Using response.body(). Found files.");
                        }
                    }
                } else {
                    // Response not successful - check error body
                    int statusCode = response.code();
                    Log.e("AiChatBot", "API returned error code: " + statusCode);

                    // For error responses, try to parse error body
                    if (statusCode == 401) {
                        // 401 Unauthorized - check if it's auth issue or if error body contains data
                        try {
                            ResponseBody errorBody = response.errorBody();
                            if (errorBody != null) {
                                String errorString = errorBody.string();
                                Log.e("AiChatBot", "401 Unauthorized - Error body: " + errorString);

                                // Try to parse error body - sometimes API returns data even with 401
                                Gson gson = new Gson();
                                JsonParser parser = new JsonParser();
                                JsonElement jsonElement = parser.parse(errorString);

                                // Check if error body contains file data
                                if (jsonElement.isJsonArray()) {
                                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                                    if (jsonArray.size() > 0) {
                                        // Has files in error response - unlock anyway
                                        hasFiles = true;
                                        Log.w("AiChatBot", "401 but found " + jsonArray.size()
                                                + " files in error response. Unlocking.");
                                    } else {
                                        // Empty array - no files
                                        Log.w("AiChatBot", "401 Unauthorized with empty array - showing locked state.");
                                    }
                                } else if (jsonElement.isJsonObject()) {
                                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                                    // Check if it has fileId (single file)
                                    if (jsonObject.has("fileId")) {
                                        hasFiles = true;
                                        Log.w("AiChatBot", "401 but found file in error response. Unlocking.");
                                    }
                                    // Check if it has "message" field indicating auth error
                                    else if (jsonObject.has("message")) {
                                        String message = jsonObject.get("message").getAsString();
                                        Log.w("AiChatBot", "401 Unauthorized - Auth issue: " + message);
                                        // This is likely auth issue, but check SharedPreferences for token
                                        SharedPreferences prefs = getSharedPreferences("user_prefs",
                                                Context.MODE_PRIVATE);
                                        String token = prefs.getString("user_token", null);
                                        if (token == null || token.isEmpty()) {
                                            token = prefs.getString("token", null);
                                        }
                                        if (token == null || token.isEmpty()) {
                                            Log.e("AiChatBot",
                                                    "No token found in SharedPreferences! This is an auth issue.");
                                        } else {
                                            Log.w("AiChatBot",
                                                    "Token exists but API returned 401 - token may be expired or invalid.");
                                        }
                                        // Show locked state for security
                                        showLockedState();
                                        return;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("AiChatBot", "Error reading/parsing 401 error body: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else if (statusCode == 503) {
                        // 503 Service Temporarily Unavailable - thường xảy ra sau khi upload file lớn
                        // Clear cache và retry với fresh connection
                        Log.w("AiChatBot", "503 Service Temporarily Unavailable - Clearing cache and retrying...");
                        RetrofitClient.clearCache();

                        // Retry sau 1 giây với fresh Retrofit instance
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            Log.d("AiChatBot", "Retrying API call after clearing cache...");
                            aiService = RetrofitClient.getRetrofitWithAuth(AiChatBotActivity.this)
                                    .create(AiService.class);
                            aiService.getFiles().enqueue(new Callback<List<AiFileResponse>>() {
                                @Override
                                public void onResponse(Call<List<AiFileResponse>> call,
                                        Response<List<AiFileResponse>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        List<AiFileResponse> files = response.body();
                                        if (files != null && !files.isEmpty()) {
                                            Log.d("AiChatBot", "Retry successful! Found " + files.size() + " files.");
                                            showChatInterface();
                                        } else {
                                            Log.d("AiChatBot", "Retry successful but no files found.");
                                            showLockedState();
                                        }
                                    } else {
                                        Log.e("AiChatBot", "Retry failed with code: " + response.code());
                                        showLockedState();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<AiFileResponse>> call, Throwable t) {
                                    Log.e("AiChatBot", "Retry failed: " + t.getMessage());
                                    showLockedState();
                                }
                            });
                        }, 1000); // Retry sau 1 giây

                        // Tạm thời show locked state trong khi retry
                        showLockedState();
                        return;
                    } else {
                        // For other error codes, try to parse error body
                        try {
                            ResponseBody errorBody = response.errorBody();
                            if (errorBody != null) {
                                String errorString = errorBody.string();
                                Log.d("AiChatBot",
                                        "Error code " + statusCode + " - Trying to parse error body: " + errorString);

                                // Sometimes API might return data even with error status
                                Gson gson = new Gson();
                                JsonParser parser = new JsonParser();
                                JsonElement jsonElement = parser.parse(errorString);

                                if (jsonElement.isJsonArray()) {
                                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                                    if (jsonArray.size() > 0) {
                                        hasFiles = true;
                                        Log.d("AiChatBot", "Found files in error response (array).");
                                    }
                                } else if (jsonElement.isJsonObject()) {
                                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                                    if (jsonObject.has("fileId")) {
                                        hasFiles = true;
                                        Log.d("AiChatBot", "Found file in error response (single object).");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("AiChatBot", "Error parsing error body: " + e.getMessage());
                        }
                    }
                }

                if (hasFiles) {
                    Log.d("AiChatBot", "User has files. Unlocking chat interface.");
                    showChatInterface();
                } else {
                    Log.d("AiChatBot", "No files found. Showing locked state.");
                    showLockedState();
                }
            }

            @Override
            public void onFailure(Call<List<AiFileResponse>> call, Throwable t) {
                // Network error - show locked state for safety
                Log.e("AiChatBot", "Error checking files: " + t.getMessage());
                t.printStackTrace();
                showLockedState();
            }
        });
    }

    /**
     * Show chat interface and hide locked state
     */
    private void showChatInterface() {
        if (mainContentLayout != null) {
            mainContentLayout.setVisibility(View.VISIBLE);
        }
        if (lockedStateLayout != null) {
            lockedStateLayout.setVisibility(View.GONE);
        }

        // Setup chat components
        setupRecyclerView();
        setupSidebarRecyclerView();
        setupClickListeners();
        setupBottomNavigation();

        // Load chat sessions
        loadChatSessions();

        // Add welcome message if no session selected
        if (sessionId == null || sessionId.isEmpty()) {
            // Clear selection when starting new chat
            if (sessionAdapter != null) {
                sessionAdapter.setSelectedSessionId(null);
            }
            addWelcomeMessage();
        } else {
            // If sessionId exists, highlight it in sidebar after sessions are loaded
            // This will be called in loadChatSessions callback
        }
    }

    /**
     * Show locked state and hide chat interface
     */
    private void showLockedState() {
        if (mainContentLayout != null) {
            mainContentLayout.setVisibility(View.GONE);
        }
        if (lockedStateLayout != null) {
            lockedStateLayout.setVisibility(View.VISIBLE);
        }

        // Setup locked state bottom navigation
        if (lockedBottomNavComponent != null) {
            lockedBottomNavComponent.setSelectedItem(R.id.nav_ai_bot);
        }

        // Setup Import File button click listener
        if (buttonImportFile != null) {
            buttonImportFile.setOnClickListener(v -> {
                // Navigate to Import Activity
                Intent intent = new Intent(AiChatBotActivity.this, ImportActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadChatSessions() {
        if (aiService == null) {
            return;
        }

        aiService.getSessions().enqueue(new Callback<List<ChatSessionItemResponse>>() {
            @Override
            public void onResponse(Call<List<ChatSessionItemResponse>> call,
                    Response<List<ChatSessionItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatSessionItemResponse> sessions = response.body();
                    if (sessions != null) {
                        chatSessions = sessions;
                        sessionAdapter.updateSessions(chatSessions);

                        // Show/hide empty state
                        if (chatSessions.isEmpty()) {
                            textEmptyChatHistory.setVisibility(View.VISIBLE);
                            recyclerViewChatSessions.setVisibility(View.GONE);
                        } else {
                            textEmptyChatHistory.setVisibility(View.GONE);
                            recyclerViewChatSessions.setVisibility(View.VISIBLE);
                        }
                        Log.d("AiChatBot", "Loaded " + chatSessions.size() + " chat sessions");

                        // Highlight current session if exists
                        if (sessionId != null && !sessionId.isEmpty() && sessionAdapter != null) {
                            sessionAdapter.setSelectedSessionId(sessionId);
                        }
                    }
                } else {
                    Log.e("AiChatBot", "Failed to load sessions: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ChatSessionItemResponse>> call, Throwable t) {
                Log.e("AiChatBot", "Error loading sessions: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void loadSessionMessages(String targetSessionId) {
        if (aiService == null || targetSessionId == null || targetSessionId.isEmpty()) {
            return;
        }

        this.sessionId = targetSessionId;
        chatMessages.clear();
        chatAdapter.notifyDataSetChanged();

        // Update selected session in sidebar
        if (sessionAdapter != null) {
            sessionAdapter.setSelectedSessionId(targetSessionId);
        }

        aiService.getSession(targetSessionId).enqueue(new Callback<ChatSessionDetailResponse>() {
            @Override
            public void onResponse(Call<ChatSessionDetailResponse> call, Response<ChatSessionDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatSessionDetailResponse session = response.body();
                    if (session.messages != null && !session.messages.isEmpty()) {
                        // Convert session messages to ChatMessage objects
                        // message field là JSON string cần parse: {"type": "human/ai", "content":
                        // "..."}
                        Gson gson = new Gson();

                        for (ChatSessionDetailResponse.SessionMessage msg : session.messages) {
                            try {
                                // Parse message JSON string
                                ChatSessionDetailResponse.ParsedMessage parsedMsg = gson.fromJson(msg.message,
                                        ChatSessionDetailResponse.ParsedMessage.class);

                                if (parsedMsg != null && parsedMsg.content != null) {
                                    // Determine message type: "human" = user, "ai" = bot
                                    int type = "human".equals(parsedMsg.type) ? ChatMessage.TYPE_USER
                                            : ChatMessage.TYPE_BOT;
                                    ChatMessage chatMsg = new ChatMessage(parsedMsg.content, type,
                                            System.currentTimeMillis());
                                    chatMessages.add(chatMsg);
                                }
                            } catch (Exception e) {
                                Log.e("AiChatBot", "Error parsing message JSON: " + e.getMessage());
                                Log.e("AiChatBot", "Message string: " + msg.message);
                                // Skip this message if parsing fails
                            }
                        }

                        chatAdapter.notifyDataSetChanged();
                        if (chatMessages.size() > 0) {
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                        }
                        Log.d("AiChatBot",
                                "Loaded " + chatMessages.size() + " messages from session " + targetSessionId);
                    } else {
                        Log.d("AiChatBot", "Session has no messages");
                    }
                } else {
                    Log.e("AiChatBot", "Failed to load session messages: " + response.code());
                    Toast.makeText(AiChatBotActivity.this, "Không thể tải lịch sử chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatSessionDetailResponse> call, Throwable t) {
                Log.e("AiChatBot", "Error loading session messages: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(AiChatBotActivity.this, "Lỗi khi tải lịch sử chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return System.currentTimeMillis();
        }
        try {
            // Parse ISO 8601 format: "2025-01-15T10:30:00Z"
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                    java.util.Locale.US);
            return format.parse(timestamp).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
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
                System.currentTimeMillis());
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
                System.currentTimeMillis());
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Clear input
        editTextMessage.setText("");

        // Add loading message
        ChatMessage loadingMessage = new ChatMessage(
                "",
                ChatMessage.TYPE_LOADING,
                System.currentTimeMillis());
        chatMessages.add(loadingMessage);
        int loadingPosition = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(loadingPosition);
        recyclerView.scrollToPosition(loadingPosition);

        // Show loading indicator
        showLoading(true);

        // Send to API
        AiChatRequest request = new AiChatRequest(message, sessionId, userId);
        Log.d("AiChatBot", "Sending request: " + request.getMessage() + " | SessionId: " + request.getSessionId()
                + " | UserId: " + request.getUserId());

        aiChatService.sendMessage(request).enqueue(new Callback<List<AiChatResponse>>() {
            @Override
            public void onResponse(Call<List<AiChatResponse>> call, Response<List<AiChatResponse>> response) {
                showLoading(false);

                // Remove loading message
                removeLoadingMessage(loadingPosition);

                Log.d("AiChatBot", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String botResponse = response.body().get(0).getOutput();
                    Log.d("AiChatBot", "Bot response: " + botResponse);

                    if (botResponse != null && !botResponse.isEmpty()) {
                        ChatMessage botMessage = new ChatMessage(
                                botResponse,
                                ChatMessage.TYPE_BOT,
                                System.currentTimeMillis());
                        chatMessages.add(botMessage);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.scrollToPosition(chatMessages.size() - 1);

                        // Reload sessions to update sidebar
                        loadChatSessions();
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

                // Remove loading message
                removeLoadingMessage(loadingPosition);

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

    /**
     * Remove loading message from chat
     * Tìm và xóa loading message gần nhất (thường là message cuối cùng)
     */
    private void removeLoadingMessage(int expectedPosition) {
        // Tìm loading message từ cuối lên (thường là message mới nhất)
        for (int i = chatMessages.size() - 1; i >= 0; i--) {
            ChatMessage message = chatMessages.get(i);
            if (message.getType() == ChatMessage.TYPE_LOADING) {
                chatMessages.remove(i);
                chatAdapter.notifyItemRemoved(i);
                // Notify adapter that items after removed position have changed
                if (i < chatMessages.size()) {
                    chatAdapter.notifyItemRangeChanged(i, chatMessages.size() - i);
                }
                break;
            }
        }
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
                System.currentTimeMillis());
        chatMessages.add(errorMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }
}
