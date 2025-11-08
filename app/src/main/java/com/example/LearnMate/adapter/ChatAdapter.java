package com.example.LearnMate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.R;
import com.example.LearnMate.model.ChatMessage;
import com.example.LearnMate.util.MarkdownHelper;
import com.example.LearnMate.util.MarkdownWithMathHelper;
import android.webkit.WebView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_LOADING = 3;
    
    private List<ChatMessage> messages;
    
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.getType() == ChatMessage.TYPE_USER) {
            return VIEW_TYPE_USER;
        } else if (message.getType() == ChatMessage.TYPE_LOADING) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_BOT;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = inflater.inflate(R.layout.item_chat_loading, parent, false);
            return new LoadingMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        } else if (holder instanceof LoadingMessageViewHolder) {
            ((LoadingMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    // ViewHolder for user messages
    public static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private TextView textTime;
        
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }
        
        public void bind(ChatMessage message) {
            textMessage.setText(message.getMessage());
            textTime.setText(formatTime(message.getTimestamp()));
        }
    }
    
    // ViewHolder for bot messages
    public class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private WebView webViewMessage;
        private TextView textTime;
        
        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            webViewMessage = itemView.findViewById(R.id.webViewMessage);
            textTime = itemView.findViewById(R.id.textTime);
            
            // Setup WebView for MathJax rendering
            if (webViewMessage != null) {
                MarkdownWithMathHelper.setupWebViewForMath(webViewMessage);
            }
        }
        
        public void bind(ChatMessage message) {
            String messageText = message.getMessage();
            
            if (messageText != null && !messageText.isEmpty()) {
                // ALWAYS use WebView for bot messages to ensure consistent markdown and LaTeX rendering
                // This fixes issues with LaTeX formulas and complex markdown
                if (webViewMessage != null) {
                    // Use WebView for rendering with MathJax and markdown
                    textMessage.setVisibility(android.view.View.GONE);
                    webViewMessage.setVisibility(android.view.View.VISIBLE);
                    MarkdownWithMathHelper.renderMarkdownWithMath(webViewMessage, messageText);
                    
                    // Update textTime constraint to be below webViewMessage
                    android.view.ViewGroup.LayoutParams timeParams = textTime.getLayoutParams();
                    if (timeParams instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams = 
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) timeParams;
                        constraintParams.topToBottom = webViewMessage.getId();
                        textTime.setLayoutParams(constraintParams);
                    }
                } else {
                    // Use TextView for simple plain text (no markdown, no LaTeX)
                    if (webViewMessage != null) {
                        webViewMessage.setVisibility(android.view.View.GONE);
                        // Clear WebView to prevent memory leaks
                        webViewMessage.loadData("", "text/html", "UTF-8");
                    }
                    textMessage.setVisibility(android.view.View.VISIBLE);
                    textMessage.setAutoLinkMask(0); // Disable auto-linking to avoid conflicts
                    textMessage.setText(messageText); // Plain text, no markdown processing
                    
                    // Update textTime constraint to be below textMessage
                    android.view.ViewGroup.LayoutParams timeParams = textTime.getLayoutParams();
                    if (timeParams instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams = 
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) timeParams;
                        constraintParams.topToBottom = textMessage.getId();
                        textTime.setLayoutParams(constraintParams);
                    }
                }
            } else {
                // Empty message
                if (webViewMessage != null) {
                    webViewMessage.setVisibility(android.view.View.GONE);
                }
                textMessage.setVisibility(android.view.View.VISIBLE);
                textMessage.setText("");
                
                // Update textTime constraint to be below textMessage
                android.view.ViewGroup.LayoutParams timeParams = textTime.getLayoutParams();
                if (timeParams instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams = 
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) timeParams;
                    constraintParams.topToBottom = textMessage.getId();
                    textTime.setLayoutParams(constraintParams);
                }
            }
            textTime.setText(formatTime(message.getTimestamp()));
        }
    }
    
    // ViewHolder for loading messages
    public static class LoadingMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textLoading;
        
        public LoadingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textLoading = itemView.findViewById(R.id.textLoading);
        }
        
        public void bind(ChatMessage message) {
            // Loading message doesn't need to show timestamp
            // The progress bar will animate automatically
        }
    }
    
    private static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
