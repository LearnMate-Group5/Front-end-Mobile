package com.example.LearnMate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.R;
import com.example.LearnMate.network.dto.ChatSessionItemResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.SessionViewHolder> {

    private List<ChatSessionItemResponse> sessions;
    private OnSessionClickListener listener;
    private String selectedSessionId; // Track session currently active

    public interface OnSessionClickListener {
        void onSessionClick(ChatSessionItemResponse session);
    }

    public ChatSessionAdapter() {
        this.sessions = new ArrayList<>();
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void updateSessions(List<ChatSessionItemResponse> newSessions) {
        this.sessions = newSessions != null ? newSessions : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Set the currently selected session ID to highlight it
     */
    public void setSelectedSessionId(String sessionId) {
        String previousSelected = this.selectedSessionId;
        this.selectedSessionId = sessionId;
        
        // Notify changes to update UI
        if (previousSelected != null) {
            int previousIndex = findSessionIndex(previousSelected);
            if (previousIndex >= 0) {
                notifyItemChanged(previousIndex);
            }
        }
        if (sessionId != null) {
            int currentIndex = findSessionIndex(sessionId);
            if (currentIndex >= 0) {
                notifyItemChanged(currentIndex);
            }
        }
    }

    private int findSessionIndex(String sessionId) {
        if (sessionId == null || sessions == null) {
            return -1;
        }
        for (int i = 0; i < sessions.size(); i++) {
            if (sessionId.equals(sessions.get(i).sessionId)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSessionItemResponse session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private TextView textSessionTitle;
        private TextView textSessionDate;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textSessionTitle = itemView.findViewById(R.id.textSessionTitle);
            textSessionDate = itemView.findViewById(R.id.textSessionDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSessionClick(sessions.get(position));
                }
            });
        }

        void bind(ChatSessionItemResponse session) {
            boolean isSelected = session.sessionId != null && 
                                 session.sessionId.equals(selectedSessionId);
            
            // Use title from API response
            String title = session.title != null && !session.title.trim().isEmpty() 
                ? session.title 
                : "Cuộc trò chuyện mới";
            
            // Truncate if too long
            if (title.length() > 50) {
                title = title.substring(0, 50) + "...";
            }
            textSessionTitle.setText(title);
            
            // Highlight selected session
            if (isSelected) {
                // Selected: bold text, different background, different text color
                textSessionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                textSessionTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_primary));
                textSessionDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_primary));
                itemView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.item_chat_session_selected));
            } else {
                // Normal: regular text, default background
                textSessionTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
                textSessionTitle.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                textSessionDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_hint));
                itemView.setBackgroundResource(android.R.drawable.list_selector_background);
            }

            // Format date - use lastActivityDate if available, otherwise createdDate
            String dateStr = formatDate(session.lastActivityDate != null ? session.lastActivityDate : session.createdDate);
            textSessionDate.setText(dateStr);
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) {
                return "";
            }

            try {
                // Parse ISO 8601 format: "2025-11-01T19:06:23.0532" (có thể có milliseconds)
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                // Nếu có milliseconds, parse lại
                if (dateStr.contains(".")) {
                    try {
                        inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                    } catch (Exception e) {
                        // Fallback to first format
                    }
                }
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                Date date = inputFormat.parse(dateStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // If parsing fails, return original string
                return dateStr;
            }

            return dateStr;
        }
    }
}

