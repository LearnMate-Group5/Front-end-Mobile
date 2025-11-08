package com.example.LearnMate.view;

import com.example.LearnMate.adapter.ChatSessionAdapter;
import com.example.LearnMate.model.ChatMessage;
import com.example.LearnMate.network.dto.ChatSessionItemResponse;

import java.util.List;

public interface AiChatBotView {
    void showChatInterface();
    void showLockedState();
    void setupRecyclerView();
    void setupSidebarRecyclerView(ChatSessionAdapter.OnSessionClickListener listener);
    void showLoading(boolean show);
    void setSendButtonEnabled(boolean enabled);
    void addChatMessage(ChatMessage message);
    void removeLoadingMessage();
    void clearChatMessages();
    void scrollToBottom();
    void updateChatSessions(List<ChatSessionItemResponse> sessions);
    void setSelectedSessionId(String sessionId);
    void showEmptyChatHistory(boolean show);
    void showErrorMessage(String message);
    void navigateToImport();
    void clearInput();
    String getUserId();
    ChatSessionAdapter getSessionAdapter();
}

