package com.example.LearnMate.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.LearnMate.model.AiChatBotModel;
import com.example.LearnMate.model.ChatMessage;
import com.example.LearnMate.network.dto.ChatSessionDetailResponse;
import com.example.LearnMate.network.dto.ChatSessionItemResponse;
import com.example.LearnMate.view.AiChatBotView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AiChatBotPresenter {
    private static final String TAG = "AiChatBotPresenter";
    
    private final AiChatBotView view;
    private final AiChatBotModel model;
    private final Context context;
    
    private String sessionId = "";
    private String userId;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private int loadingMessagePosition = -1;

    public AiChatBotPresenter(AiChatBotView view, AiChatBotModel model, Context context) {
        this.view = view;
        this.model = model;
        this.context = context;
        this.userId = view.getUserId();
    }

    /**
     * Check files and setup UI
     */
    public void checkFilesAndSetupUI() {
        model.checkFiles(context, new AiChatBotModel.FilesCheckCallback() {
            @Override
            public void onHasFiles() {
                view.showChatInterface();
                loadChatSessions();
                addWelcomeMessageIfNeeded();
            }

            @Override
            public void onNoFiles() {
                view.showLockedState();
            }

            @Override
            public void onRetryNeeded() {
                // Retry after delay
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    checkFilesAndSetupUI();
                }, 1000);
                view.showLockedState();
            }
        });
    }

    /**
     * Load chat sessions
     */
    public void loadChatSessions() {
        model.loadChatSessions(new AiChatBotModel.SessionsCallback() {
            @Override
            public void onSuccess(List<ChatSessionItemResponse> sessions) {
                view.updateChatSessions(sessions);
                if (sessions.isEmpty()) {
                    view.showEmptyChatHistory(true);
                } else {
                    view.showEmptyChatHistory(false);
                }
                
                // Highlight current session if exists
                if (sessionId != null && !sessionId.isEmpty()) {
                    view.setSelectedSessionId(sessionId);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading sessions: " + message);
            }
        });
    }

    /**
     * Load session messages - returns messages via callback for UI to handle
     */
    public void loadSessionMessages(String targetSessionId, SessionMessagesUiCallback uiCallback) {
        sessionId = targetSessionId;
        view.setSelectedSessionId(targetSessionId);
        
        model.loadSessionMessages(targetSessionId, new AiChatBotModel.SessionMessagesCallback() {
            @Override
            public void onSuccess(ChatSessionDetailResponse session) {
                List<ChatMessage> messages = new ArrayList<>();
                if (session.messages != null && !session.messages.isEmpty()) {
                    Gson gson = new Gson();
                    
                    for (ChatSessionDetailResponse.SessionMessage msg : session.messages) {
                        try {
                            ChatSessionDetailResponse.ParsedMessage parsedMsg = 
                                gson.fromJson(msg.message, ChatSessionDetailResponse.ParsedMessage.class);
                            
                            if (parsedMsg != null && parsedMsg.content != null) {
                                int type = "human".equals(parsedMsg.type) ? ChatMessage.TYPE_USER : ChatMessage.TYPE_BOT;
                                ChatMessage chatMsg = new ChatMessage(parsedMsg.content, type, System.currentTimeMillis());
                                messages.add(chatMsg);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing message JSON: " + e.getMessage());
                        }
                    }
                }
                
                if (uiCallback != null) {
                    uiCallback.onMessagesLoaded(messages);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading session messages: " + message);
                view.showErrorMessage("Không thể tải lịch sử chat");
            }
        });
    }

    public interface SessionMessagesUiCallback {
        void onMessagesLoaded(List<ChatMessage> messages);
    }

    /**
     * Start new chat
     */
    public void startNewChat() {
        sessionId = "";
        chatMessages.clear();
        view.clearChatMessages();
        view.setSelectedSessionId(null);
        addWelcomeMessageIfNeeded();
    }

    /**
     * Send message - returns callback for UI to handle message display
     */
    public void sendMessage(String messageText, SendMessageUiCallback uiCallback) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        // Show loading
        view.showLoading(true);
        view.setSendButtonEnabled(false);

        // Send to API
        model.sendMessage(messageText.trim(), sessionId, userId, new AiChatBotModel.SendMessageCallback() {
            @Override
            public void onSuccess(String botResponse) {
                view.showLoading(false);
                view.setSendButtonEnabled(true);
                
                // Update sessionId if it was empty (new session created)
                if ((sessionId == null || sessionId.isEmpty()) && uiCallback != null) {
                    // Session ID might be updated by backend, but we'll handle it in reload
                }
                
                // Call UI callback to add bot message
                if (uiCallback != null) {
                    uiCallback.onBotResponse(botResponse);
                }
                
                // Reload sessions to update sidebar
                loadChatSessions();
            }

            @Override
            public void onError(String errorMessage) {
                view.showLoading(false);
                view.setSendButtonEnabled(true);
                
                String chatErrorMessage;
                if (errorMessage.contains("timeout")) {
                    chatErrorMessage = "⏰ I'm taking longer than expected to process your request. Please try again!";
                } else {
                    chatErrorMessage = "Sorry, I encountered an error. Please check your network connection and try again.";
                }
                
                // Call UI callback to add error message
                if (uiCallback != null) {
                    uiCallback.onError(chatErrorMessage);
                }
            }
        });
    }

    public interface SendMessageUiCallback {
        void onBotResponse(String botResponse);
        void onError(String errorMessage);
    }

    /**
     * Add welcome message if needed
     */
    private void addWelcomeMessageIfNeeded() {
        if (sessionId == null || sessionId.isEmpty()) {
            ChatMessage welcomeMessage = new ChatMessage(
                "Hello! I'm your AI assistant. How can I help you today?",
                ChatMessage.TYPE_BOT,
                System.currentTimeMillis()
            );
            chatMessages.add(welcomeMessage);
            view.addChatMessage(welcomeMessage);
            view.scrollToBottom();
        }
    }

    /**
     * Navigate to import
     */
    public void navigateToImport() {
        view.navigateToImport();
    }

    public String getSessionId() {
        return sessionId;
    }
}

