package com.example.LearnMate.reader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Selection;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.R;
import com.example.LearnMate.managers.BookmarkManager;
import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AiHighlightService;
import com.example.LearnMate.network.dto.AiHighlightRequest;
import com.example.LearnMate.network.dto.AiHighlightResponse;
import com.example.LearnMate.util.MarkdownHelper;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReaderActivity extends AppCompatActivity {

    // Biến instance để lưu trạng thái
    private List<ChapterUtils.Chapter> chapters;
    private int currentChapterIndex = 0;
    private String currentMode = "raw";
    private float currentFontSize = 16f; // Font size mặc định
    private BookmarkManager bookmarkManager;
    private String pdfUri;
    private ImageButton btnBookmark;
    
    // AI Highlight
    private AiHighlightService aiHighlightService;
    private SessionManager sessionManager;
    private PopupWindow highlightPopup;
    private String currentSessionId;
    private String currentUserId;

    /** Helper mở Reader nhanh */
    public static void open(Context ctx, Uri pdfUri, int chapterIndex, String mode) {
        Intent i = new Intent(ctx, ReaderActivity.class);
        i.putExtra("pdf_uri", pdfUri.toString());
        i.putExtra("chapter_index", chapterIndex);
        i.putExtra("mode", mode); // "raw" | "translate"
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        // Đọc extras từ intent
        String uri = getIntent().getStringExtra("pdf_uri");
        int chapter = getIntent().getIntExtra("chapter_index", 0);
        String mode = getIntent().getStringExtra("mode");

        // Setup UI
        setupUI(uri, chapter, mode);
    }

    private void setupUI(String pdfUri, int chapterIndex, String mode) {
        // Lưu trạng thái
        this.currentMode = mode;
        this.currentChapterIndex = chapterIndex;
        this.pdfUri = pdfUri;

        // Initialize bookmark manager
        this.bookmarkManager = new BookmarkManager(this);
        this.btnBookmark = findViewById(R.id.btnBookmark);
        
        // Initialize AI Highlight
        this.sessionManager = new SessionManager(this);
        this.aiHighlightService = RetrofitClient.getAiHighlightService(this);
        
        // Get user ID và tạo session ID
        com.example.LearnMate.network.dto.LoginResponse.UserData userData = sessionManager.getUserData();
        if (userData != null && userData.getUserId() != null) {
            this.currentUserId = userData.getUserId();
        } else {
            this.currentUserId = UUID.randomUUID().toString();
        }
        this.currentSessionId = UUID.randomUUID().toString();
        
        // Setup text selection for highlighting
        setupTextSelection();

        // Lấy dữ liệu từ ContentCache - chỉ dùng dữ liệu thật từ API
        if (!ContentCache.hasRealData()) {
            if (ContentCache.isWaitingForData()) {
                showWaitingMessage();
            } else {
                showNoDataMessage();
            }
            return;
        }

        // Sử dụng RAW chapters (có cả raw và translated content)
        this.chapters = ContentCache.RAW;

        // Nếu không có dữ liệu, hiển thị thông báo
        if (chapters == null || chapters.isEmpty()) {
            showNoDataMessage();
            return;
        }

        // Đảm bảo chapter index hợp lệ
        if (chapterIndex < 0 || chapterIndex >= chapters.size()) {
            chapterIndex = 0;
            this.currentChapterIndex = 0;
        }

        // Cập nhật UI
        updateChapterDisplay();

        // Setup navigation buttons
        setupNavigationButtons();

        // Setup font size control
        setupFontSizeControl();

        // Setup back button
        setupBackButton();

        // Setup menu button
        setupMenuButton();

        // Setup bookmark button
        setupBookmarkButton();
    }

    private void showNoDataMessage() {
        TextView tvContent = findViewById(R.id.tvContent);
        if (tvContent != null) {
            tvContent.setText("Chưa có dữ liệu. Vui lòng upload PDF và chờ xử lý hoàn tất.");
        }
    }

    private void showWaitingMessage() {
        TextView tvContent = findViewById(R.id.tvContent);
        if (tvContent != null) {
            tvContent.setText("Đang xử lý PDF... Vui lòng chờ trong giây lát. Dữ liệu sẽ được cập nhật tự động.");
        }
    }

    private void updateChapterDisplay() {
        if (chapters == null || chapters.isEmpty())
            return;

        ChapterUtils.Chapter currentChapter = chapters.get(currentChapterIndex);

        // Cập nhật UI
        TextView tvBookTitle = findViewById(R.id.tvBookTitle);
        TextView tvChapterTitle = findViewById(R.id.tvChapterTitle);
        TextView tvContent = findViewById(R.id.tvContent);

        if (tvBookTitle != null) {
            tvBookTitle.setText("PDF Document");
        }

        if (tvChapterTitle != null) {
            tvChapterTitle.setText(currentChapter.title);
        }

        if (tvContent != null) {
            // Hiển thị nội dung dựa trên mode hiện tại
            String contentToDisplay;
            if ("translate".equals(currentMode)) {
                // Hiển thị nội dung đã dịch
                contentToDisplay = currentChapter.translatedContent != null ? currentChapter.translatedContent : "";
            } else {
                // Hiển thị nội dung gốc
                contentToDisplay = currentChapter.content != null ? currentChapter.content : "";
            }
            
            // Render markdown (with fallback if Markwon is not available)
            if (contentToDisplay != null && !contentToDisplay.isEmpty()) {
                MarkdownHelper.renderMarkdown(tvContent, contentToDisplay);
            } else {
                tvContent.setText("");
            }
            
            tvContent.setTextSize(currentFontSize);
            
            // Re-enable text selection after setting text
            tvContent.setTextIsSelectable(true);
        }

        // Update bookmark icon
        updateBookmarkIcon();
    }

    private void setupNavigationButtons() {
        TextView btnPrev = findViewById(R.id.btnPrev);
        TextView btnNext = findViewById(R.id.btnNext);

        if (btnPrev != null) {
            btnPrev.setEnabled(currentChapterIndex > 0);
            btnPrev.setOnClickListener(v -> {
                if (currentChapterIndex > 0) {
                    currentChapterIndex--;
                    updateChapterDisplay();
                    updateNavigationButtons();
                }
            });
        }

        if (btnNext != null) {
            btnNext.setEnabled(currentChapterIndex < chapters.size() - 1);
            btnNext.setOnClickListener(v -> {
                if (currentChapterIndex < chapters.size() - 1) {
                    currentChapterIndex++;
                    updateChapterDisplay();
                    updateNavigationButtons();
                }
            });
        }
    }

    private void updateNavigationButtons() {
        TextView btnPrev = findViewById(R.id.btnPrev);
        TextView btnNext = findViewById(R.id.btnNext);

        if (btnPrev != null) {
            btnPrev.setEnabled(currentChapterIndex > 0);
        }

        if (btnNext != null) {
            btnNext.setEnabled(currentChapterIndex < chapters.size() - 1);
        }
    }

    private void setupFontSizeControl() {
        SeekBar seekBar = findViewById(R.id.sbFont);
        if (seekBar != null) {
            // Set range từ 12sp đến 24sp
            seekBar.setMax(12); // 24 - 12 = 12
            seekBar.setProgress((int) (currentFontSize - 12));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        currentFontSize = 12 + progress; // 12sp to 24sp
                        TextView tvContent = findViewById(R.id.tvContent);
                        if (tvContent != null) {
                            tvContent.setTextSize(currentFontSize);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    private void setupBackButton() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupMenuButton() {
        ImageButton btnMore = findViewById(R.id.btnMore);
        if (btnMore != null) {
            btnMore.setOnClickListener(v -> showPopupMenu(v));
        }
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.reader_menu, popup.getMenu());

        // Cập nhật menu items dựa trên mode hiện tại
        android.view.Menu menu = popup.getMenu();
        android.view.MenuItem translateItem = menu.findItem(R.id.action_translate);

        if ("raw".equals(currentMode)) {
            // Trong raw mode: translate khả dụng, highlight và note khả dụng
            if (translateItem != null) {
                translateItem.setEnabled(true);
                translateItem.setVisible(true);
            }
        } else {
            // Trong translate mode: translate không khả dụng, highlight và note khả dụng
            if (translateItem != null) {
                translateItem.setEnabled(false);
                translateItem.setVisible(false);
            }
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_translate) {
                // Switch to translated mode - chỉ khả dụng trong raw
                if ("raw".equals(currentMode)) {
                    currentMode = "translate";
                    updateChapterDisplay();
                    Toast.makeText(this, "Switched to Translated mode", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.action_highlight) {
                // Highlight feature - khả dụng cả raw và translate
                highlightCurrentText();
                return true;
            } else if (id == R.id.action_note) {
                // Note feature - khả dụng cả raw và translate
                addNote();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void toggleTranslateMode() {
        // Toggle between raw and translated content
        if ("raw".equals(currentMode)) {
            currentMode = "translate";
            Toast.makeText(this, "Switched to Translated mode", Toast.LENGTH_SHORT).show();
        } else {
            currentMode = "raw";
            Toast.makeText(this, "Switched to Raw mode", Toast.LENGTH_SHORT).show();
        }

        // Update content display
        if (chapters != null && !chapters.isEmpty()) {
            // Sử dụng RAW chapters (có cả raw và translated content)
            this.chapters = ContentCache.RAW;
            updateChapterDisplay();
        }
    }

    private void setupTextSelection() {
        TextView tvContent = findViewById(R.id.tvContent);
        if (tvContent == null) return;
        
        // Enable text selection
        tvContent.setTextIsSelectable(true);
        
        // Setup long press listener để tự động highlight
        tvContent.setOnLongClickListener(v -> {
            int start = tvContent.getSelectionStart();
            int end = tvContent.getSelectionEnd();
            
            if (start >= 0 && end > start) {
                String selectedText = tvContent.getText().toString().substring(start, end).trim();
                if (!selectedText.isEmpty()) {
                    // Auto-trigger highlight khi long press
                    fetchHighlightInfo(selectedText, tvContent, start, end);
                    return true;
                }
            }
            return false;
        });
        
        // Setup custom ActionMode callback để handle highlight
        tvContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Thêm menu item "AI Highlight"
                MenuItem highlightItem = menu.add(0, android.R.id.copy, 0, "AI Highlight");
                highlightItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                return true;
            }
            
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }
            
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == android.R.id.copy) {
                    // Trigger highlight
                    highlightCurrentText();
                    mode.finish(); // Close action mode
                    return true;
                }
                return false;
            }
            
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Selection mode ended
            }
        });
        
        // Note: User can select text and use action menu or long press to highlight
    }

    private void highlightCurrentText() {
        TextView tvContent = findViewById(R.id.tvContent);
        if (tvContent == null) {
            Toast.makeText(this, "No content available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int start = tvContent.getSelectionStart();
        int end = tvContent.getSelectionEnd();
        
        if (start < 0 || end <= start) {
            Toast.makeText(this, "Please select text first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String selectedText = tvContent.getText().toString().substring(start, end).trim();
        if (selectedText.isEmpty()) {
            Toast.makeText(this, "Please select some text", Toast.LENGTH_SHORT).show();
            return;
        }
        
        fetchHighlightInfo(selectedText, tvContent, start, end);
    }
    
    private void fetchHighlightInfo(String selectedText, TextView tvContent, int start, int end) {
        // Log request details
        Log.d("ReaderActivity", "=== AI Highlight Request ===");
        Log.d("ReaderActivity", "Selected text: " + selectedText);
        Log.d("ReaderActivity", "SessionId: " + currentSessionId);
        Log.d("ReaderActivity", "UserId: " + currentUserId);
        
        // Tạo request
        AiHighlightRequest request = new AiHighlightRequest(
            selectedText,
            currentSessionId,
            currentUserId
        );
        
        // Show loading popup
        showHighlightPopup(selectedText, null, true, tvContent, start, end);
        
        // Call API
        aiHighlightService.getHighlightInfo(request).enqueue(new Callback<List<AiHighlightResponse>>() {
            @Override
            public void onResponse(Call<List<AiHighlightResponse>> call, Response<List<AiHighlightResponse>> response) {
                Log.d("ReaderActivity", "=== AI Highlight Response ===");
                Log.d("ReaderActivity", "Response code: " + response.code());
                Log.d("ReaderActivity", "Response isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Extract output từ response (n8n trả về array)
                    List<AiHighlightResponse> responseList = response.body();
                    AiHighlightResponse highlightResponse = responseList.get(0);
                    String info = highlightResponse != null ? highlightResponse.getOutput() : null;
                    
                    Log.d("ReaderActivity", "Response list size: " + responseList.size());
                    Log.d("ReaderActivity", "Response body: " + (highlightResponse != null ? highlightResponse.toString() : "null"));
                    Log.d("ReaderActivity", "Output: " + (info != null && info.length() > 0 ? info.substring(0, Math.min(100, info.length())) + "..." : "null"));
                    
                    // Try alternative parsing if output is null
                    if (info == null || info.isEmpty()) {
                        Log.w("ReaderActivity", "Output is null or empty, trying to extract from response");
                        // Try to get from first item in array
                        if (!responseList.isEmpty()) {
                            Object firstItem = responseList.get(0);
                            if (firstItem instanceof AiHighlightResponse) {
                                AiHighlightResponse item = (AiHighlightResponse) firstItem;
                                info = item.getOutput();
                            } else {
                                // Try direct string
                                info = firstItem.toString();
                            }
                        }
                    }
                    
                    if (info == null || info.isEmpty()) {
                        info = "No information available for: " + selectedText;
                        Log.w("ReaderActivity", "No info extracted, using fallback message");
                    } else {
                        Log.d("ReaderActivity", "Successfully extracted info, length: " + info.length());
                    }
                    
                    // Show popup with info
                    showHighlightPopup(selectedText, info, false, tvContent, start, end);
                    
                    // Highlight text visually
                    highlightTextInView(tvContent, start, end);
                } else {
                    String errorMsg = "Failed to get information";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required. Please login.";
                    } else if (response.code() == 404) {
                        errorMsg = "Endpoint not found. Please check n8n webhook configuration.";
                    } else if (response.code() >= 500) {
                        errorMsg = "Server error. Please try again later.";
                    } else if (response.body() == null || response.body().isEmpty()) {
                        errorMsg = "Empty response from server. Please check n8n workflow.";
                    }
                    
                    Log.e("ReaderActivity", "Highlight API error: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("ReaderActivity", "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("ReaderActivity", "Error reading error body", e);
                        }
                    }
                    
                    showHighlightPopup(selectedText, errorMsg, false, tvContent, start, end);
                }
            }
            
            @Override
            public void onFailure(Call<List<AiHighlightResponse>> call, Throwable t) {
                String errorMsg = "Network error. Please check your connection.";
                if (t.getMessage() != null) {
                    Log.e("ReaderActivity", "Failure message: " + t.getMessage());
                    if (t.getMessage().contains("timeout")) {
                        errorMsg = "Request timeout. Please try again.";
                    } else if (t.getMessage().contains("Failed to connect") || t.getMessage().contains("Unable to resolve host")) {
                        errorMsg = "Cannot connect to server. Please check if n8n is running on port 5678.";
                    } else if (t.getMessage().contains("404")) {
                        errorMsg = "Webhook not found. Please check n8n webhook path: webhook/ai-highlight";
                    }
                }
                showHighlightPopup(selectedText, errorMsg, false, tvContent, start, end);
                Log.e("ReaderActivity", "Highlight API failure", t);
                t.printStackTrace();
            }
        });
    }
    
    private void highlightTextInView(TextView tv, int start, int end) {
        // Visual highlight sẽ được handle bởi selection, không cần thêm
        // Chỉ cần đảm bảo selection vẫn được giữ
        tv.post(() -> {
            if (tv.getText() instanceof android.text.Spannable) {
                Selection.setSelection((android.text.Spannable) tv.getText(), start, end);
            }
        });
    }
    
    private void showHighlightPopup(String highlightedWord, String info, boolean isLoading, 
                                     TextView tvContent, int selectionStart, int selectionEnd) {
        // Dismiss existing popup
        if (highlightPopup != null && highlightPopup.isShowing()) {
            highlightPopup.dismiss();
        }
        
        // Inflate popup layout
        View popupView = getLayoutInflater().inflate(R.layout.popup_highlight_info, null);
        
        TextView tvWord = popupView.findViewById(R.id.tvHighlightedWord);
        TextView tvInfo = popupView.findViewById(R.id.tvHighlightInfo);
        ScrollView svContent = popupView.findViewById(R.id.svContent);
        ProgressBar pbLoading = popupView.findViewById(R.id.pbLoading);
        TextView tvError = popupView.findViewById(R.id.tvError);
        ImageButton btnClose = popupView.findViewById(R.id.btnClose);
        
        // Set highlighted word
        if (tvWord != null) {
            tvWord.setText(highlightedWord);
        }
        
        // Handle loading/error/success states
        if (isLoading) {
            if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
            if (svContent != null) svContent.setVisibility(View.GONE);
            if (tvError != null) tvError.setVisibility(View.GONE);
        } else if (info == null || info.isEmpty()) {
            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
            if (svContent != null) svContent.setVisibility(View.GONE);
            if (tvError != null) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("No information available");
            }
        } else {
            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
            if (svContent != null) svContent.setVisibility(View.VISIBLE);
            if (tvError != null) tvError.setVisibility(View.GONE);
            if (tvInfo != null) {
                tvInfo.setText(info);
            }
        }
        
        // Close button
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                if (highlightPopup != null && highlightPopup.isShowing()) {
                    highlightPopup.dismiss();
                }
            });
        }
        
        // Create and show popup near selected text
        if (tvContent != null && tvContent.getLayout() != null && 
            selectionStart >= 0 && selectionEnd > selectionStart) {
            try {
                // Get line info for selected text
                int lineStart = tvContent.getLayout().getLineForOffset(selectionStart);
                int lineEnd = tvContent.getLayout().getLineForOffset(selectionEnd);
                
                // Use the start line for positioning
                int lineBottom = tvContent.getLayout().getLineBottom(lineStart);
                float xStart = tvContent.getLayout().getPrimaryHorizontal(selectionStart);
                float xEnd = tvContent.getLayout().getPrimaryHorizontal(selectionEnd);
                float xCenter = (xStart + xEnd) / 2;
                
                // Get TextView position on screen
                int[] location = new int[2];
                tvContent.getLocationOnScreen(location);
                
                // Calculate popup position
                int popupWidth = (int) (300 * getResources().getDisplayMetrics().density); // 300dp
                int popupX = location[0] + (int) xCenter - (popupWidth / 2);
                int popupY = location[1] + lineBottom + (int) (16 * getResources().getDisplayMetrics().density); // 16dp offset
                
                // Ensure popup doesn't go off screen
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                if (popupX < 0) {
                    popupX = (int) (16 * getResources().getDisplayMetrics().density); // 16dp margin
                } else if (popupX + popupWidth > screenWidth) {
                    popupX = screenWidth - popupWidth - (int) (16 * getResources().getDisplayMetrics().density);
                }
                
                // Ensure popup doesn't go below screen
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int maxPopupHeight = (int) (500 * getResources().getDisplayMetrics().density); // 500dp max
                if (popupY + maxPopupHeight > screenHeight) {
                    // Show above text instead
                    int lineTop = tvContent.getLayout().getLineTop(lineStart);
                    popupY = location[1] + lineTop - maxPopupHeight - (int) (16 * getResources().getDisplayMetrics().density);
                    if (popupY < 0) {
                        popupY = (int) (16 * getResources().getDisplayMetrics().density);
                    }
                }
                
                highlightPopup = new PopupWindow(
                    popupView,
                    popupWidth,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                );
                
                highlightPopup.setOutsideTouchable(true);
                highlightPopup.setFocusable(true);
                highlightPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));
                highlightPopup.setElevation(8);
                
                // Show popup at calculated position
                highlightPopup.showAtLocation(
                    findViewById(android.R.id.content),
                    android.view.Gravity.NO_GRAVITY,
                    popupX,
                    popupY
                );
                
                Log.d("ReaderActivity", "Popup shown at: (" + popupX + ", " + popupY + ")");
                
            } catch (Exception e) {
                Log.e("ReaderActivity", "Error showing popup at text position", e);
                // Fallback: show at center
                showPopupAtCenter(popupView);
            }
        } else {
            // Fallback: show at center
            showPopupAtCenter(popupView);
        }
    }
    
    private void showPopupAtCenter(View popupView) {
        highlightPopup = new PopupWindow(
            popupView,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        highlightPopup.setOutsideTouchable(true);
        highlightPopup.setFocusable(true);
        highlightPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));
        highlightPopup.setElevation(8);
        highlightPopup.showAtLocation(
            findViewById(android.R.id.content),
            android.view.Gravity.CENTER,
            0,
            0
        );
    }

    private void addNote() {
        // TODO: Implement note adding functionality
        Toast.makeText(this, "Note feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void setupBookmarkButton() {
        if (btnBookmark == null) {
            return;
        }

        // Update bookmark icon dựa trên trạng thái hiện tại
        updateBookmarkIcon();

        // Handle click
        btnBookmark.setOnClickListener(v -> {
            if (chapters == null || chapters.isEmpty()) {
                return;
            }

            ChapterUtils.Chapter currentChapter = chapters.get(currentChapterIndex);
            String bookTitle = getIntent().getStringExtra("book_title");

            // Toggle bookmark
            boolean added = bookmarkManager.toggleBookmark(
                    pdfUri != null ? pdfUri : "",
                    bookTitle != null ? bookTitle : "Unknown",
                    currentChapterIndex,
                    currentChapter.title);

            // Update icon
            updateBookmarkIcon();

            // Show toast
            String message = added ? "Bookmarked: " + currentChapter.title : "Bookmark removed";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateBookmarkIcon() {
        if (btnBookmark == null || pdfUri == null) {
            return;
        }

        boolean isBookmarked = bookmarkManager.isBookmarked(pdfUri, currentChapterIndex);

        if (isBookmarked) {
            btnBookmark.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnBookmark.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
}
