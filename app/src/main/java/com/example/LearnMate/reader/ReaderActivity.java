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
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

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
import com.example.LearnMate.util.MarkdownWithMathHelper;

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
    private ImageButton btnSound;
    
    // AI Highlight
    private AiHighlightService aiHighlightService;
    private SessionManager sessionManager;
    private PopupWindow highlightPopup;
    private String currentSessionId;
    private String currentUserId;
    
    // Text-to-Speech
    private android.media.MediaPlayer mediaPlayer;
    private boolean isPlayingAudio = false;
    
    // WebView for content display
    private WebView webViewContent;
    
    // JavaScript interface for text selection
    private class WebAppInterface {
        @JavascriptInterface
        public void onTextSelected(String selectedText) {
            runOnUiThread(() -> {
                if (selectedText != null && !selectedText.trim().isEmpty()) {
                    fetchHighlightInfo(selectedText.trim(), webViewContent, 0, 0);
                }
            });
        }
    }

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
        
        // Setup WebView for content display (only once) - must be called before showing messages
        setupWebView();
        
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
        
        // Setup sound button (TTS)
        setupSoundButton();
    }

    private void showNoDataMessage() {
        if (webViewContent != null) {
            MarkdownWithMathHelper.renderMarkdownWithMath(webViewContent, "Chưa có dữ liệu. Vui lòng upload PDF và chờ xử lý hoàn tất.");
        }
    }

    private void showWaitingMessage() {
        if (webViewContent != null) {
            MarkdownWithMathHelper.renderMarkdownWithMath(webViewContent, "Đang xử lý PDF... Vui lòng chờ trong giây lát. Dữ liệu sẽ được cập nhật tự động.");
        }
    }

    private void updateChapterDisplay() {
        if (chapters == null || chapters.isEmpty())
            return;

        ChapterUtils.Chapter currentChapter = chapters.get(currentChapterIndex);

        // Cập nhật UI
        TextView tvBookTitle = findViewById(R.id.tvBookTitle);
        TextView tvChapterTitle = findViewById(R.id.tvChapterTitle);
        webViewContent = findViewById(R.id.webViewContent);

        if (tvBookTitle != null) {
            tvBookTitle.setText("PDF Document");
        }

        if (tvChapterTitle != null) {
            tvChapterTitle.setText(currentChapter.title);
        }

        if (webViewContent != null) {
            // Hiển thị nội dung dựa trên mode hiện tại
            String contentToDisplay;
            if ("translate".equals(currentMode)) {
                contentToDisplay = currentChapter.translatedContent != null ? currentChapter.translatedContent : "";
            } else {
                contentToDisplay = currentChapter.content != null ? currentChapter.content : "";
            }
            
            // Render markdown with MathJax
            if (contentToDisplay != null && !contentToDisplay.isEmpty()) {
                MarkdownWithMathHelper.renderMarkdownWithMath(webViewContent, contentToDisplay);
                // Inject text selection script and apply font size after a delay to ensure page is loaded
                webViewContent.postDelayed(() -> {
                    injectTextSelectionScript(webViewContent);
                    applyFontSize(webViewContent, currentFontSize);
                }, 1000);
                // Also inject after longer delay to catch MathJax rendering
                webViewContent.postDelayed(() -> {
                    injectTextSelectionScript(webViewContent);
                    applyFontSize(webViewContent, currentFontSize);
                }, 3000);
            } else {
                webViewContent.loadData("", "text/html", "UTF-8");
            }
        }

        // Update bookmark icon
        updateBookmarkIcon();
    }
    
    /**
     * Inject JavaScript for text selection in WebView
     */
    private void injectTextSelectionScript(WebView webView) {
        String script = 
            "(function() {" +
            "  if (window.textSelectionHandlerAdded) return;" +
            "  window.textSelectionHandlerAdded = true;" +
            "  " +
            "  var lastSelection = '';" +
            "  var selectionTimer = null;" +
            "  " +
            "  function checkSelection() {" +
            "    var selection = window.getSelection();" +
            "    var selectedText = selection.toString().trim();" +
            "    " +
            "    if (selectedText.length > 0 && selectedText !== lastSelection) {" +
            "      lastSelection = selectedText;" +
            "      if (window.Android && window.Android.onTextSelected) {" +
            "        window.Android.onTextSelected(selectedText);" +
            "      }" +
            "    }" +
            "  }" +
            "  " +
            "  document.addEventListener('mouseup', function() {" +
            "    clearTimeout(selectionTimer);" +
            "    selectionTimer = setTimeout(checkSelection, 100);" +
            "  });" +
            "  " +
            "  document.addEventListener('touchend', function() {" +
            "    clearTimeout(selectionTimer);" +
            "    selectionTimer = setTimeout(checkSelection, 300);" +
            "  });" +
            "})();";
        
        webView.evaluateJavascript(script, null);
    }
    
    /**
     * Apply font size to WebView content
     */
    private void applyFontSize(WebView webView, float fontSize) {
        if (webView == null) return;
        
        String script = String.format(
            "(function() {" +
            "  var styleId = 'custom-font-size-style';" +
            "  var existingStyle = document.getElementById(styleId);" +
            "  if (existingStyle) {" +
            "    existingStyle.remove();" +
            "  }" +
            "  var style = document.createElement('style');" +
            "  style.id = styleId;" +
            "  style.innerHTML = 'body { font-size: %fpx !important; }';" +
            "  document.head.appendChild(style);" +
            "})();",
            fontSize
        );
        webView.evaluateJavascript(script, null);
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
                        WebView webViewContent = findViewById(R.id.webViewContent);
                        if (webViewContent != null) {
                            applyFontSize(webViewContent, currentFontSize);
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
        // Text selection in WebView is handled via JavaScript interface
        // The JavaScript will call onTextSelected when text is selected
        // This is set up in setupWebView()
    }
    
    /**
     * Setup WebView for content display (only called once)
     */
    private void setupWebView() {
        webViewContent = findViewById(R.id.webViewContent);
        if (webViewContent == null) return;
        
        // Setup WebView for MathJax rendering
        MarkdownWithMathHelper.setupWebViewForMath(webViewContent);
        
        // Add JavaScript interface for text selection (must be added before loading content)
        webViewContent.addJavascriptInterface(new WebAppInterface(), "Android");
    }

    private void highlightCurrentText() {
        WebView webViewContent = findViewById(R.id.webViewContent);
        if (webViewContent == null) {
            Toast.makeText(this, "No content available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected text from WebView via JavaScript
        webViewContent.evaluateJavascript(
            "(function() {" +
            "  var selection = window.getSelection();" +
            "  return selection.toString().trim();" +
            "})();",
            new android.webkit.ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    // Remove quotes from JavaScript string result
                    if (value != null && value.length() > 2) {
                        String selectedText = value.substring(1, value.length() - 1)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .trim();
                        if (!selectedText.isEmpty()) {
                            fetchHighlightInfo(selectedText, webViewContent, 0, 0);
                        } else {
                            Toast.makeText(ReaderActivity.this, "Please select some text", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ReaderActivity.this, "Please select text first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
    
    private void fetchHighlightInfo(String selectedText, WebView webViewContent, int start, int end) {
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
        showHighlightPopup(selectedText, null, true, webViewContent, start, end);
        
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
                    showHighlightPopup(selectedText, info, false, webViewContent, start, end);
                    
                    // Note: WebView text highlighting is handled by browser selection, no need to manually highlight
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
                    
                    showHighlightPopup(selectedText, errorMsg, false, webViewContent, start, end);
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
                showHighlightPopup(selectedText, errorMsg, false, webViewContent, start, end);
                Log.e("ReaderActivity", "Highlight API failure", t);
                t.printStackTrace();
            }
        });
    }
    
    private void showHighlightPopup(String highlightedWord, String info, boolean isLoading, 
                                     WebView webViewContent, int selectionStart, int selectionEnd) {
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
        
        // For WebView, show popup at center (positioning near selected text is complex in WebView)
        // Fallback: show at center
        showPopupAtCenter(popupView);
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
    
    /**
     * Setup sound button for Text-to-Speech
     */
    private void setupSoundButton() {
        btnSound = findViewById(R.id.btnSound);
        if (btnSound == null) {
            return;
        }
        
        btnSound.setOnClickListener(v -> {
            if (isPlayingAudio) {
                stopAudio();
            } else {
                // Show loading immediately when button is clicked
                ProgressBar progress = findViewById(R.id.progress);
                if (progress != null) {
                    progress.setVisibility(View.VISIBLE);
                }
                btnSound.setEnabled(false); // Disable button while loading
                playChapterAudio();
            }
        });
    }
    
    /**
     * Convert chapter text to speech and play
     */
    private void playChapterAudio() {
        ProgressBar progress = findViewById(R.id.progress);
        
        if (chapters == null || chapters.isEmpty()) {
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            if (btnSound != null) {
                btnSound.setEnabled(true);
            }
            Toast.makeText(this, "No chapter content to read", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ChapterUtils.Chapter currentChapter = chapters.get(currentChapterIndex);
        String chapterContent = currentMode.equals("translate") && currentChapter.translatedContent != null && !currentChapter.translatedContent.isEmpty()
                ? currentChapter.translatedContent
                : currentChapter.content;
        
        if (chapterContent == null || chapterContent.trim().isEmpty()) {
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            if (btnSound != null) {
                btnSound.setEnabled(true);
            }
            Toast.makeText(this, "Chapter content is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Prepare TTS request
        com.example.LearnMate.network.dto.TTSRequest request = new com.example.LearnMate.network.dto.TTSRequest(
            chapterContent,
            1.2f,  // speed
            "2b277023-e5e6-42af-bcd7-c1841f19527b",  // hardcoded voiceId
            currentUserId,
            String.valueOf(currentChapterIndex)  // Use chapter index as uniqueId
        );
        
        Log.d("ReaderActivity", "=== TTS Request ===");
        Log.d("ReaderActivity", "Text length: " + chapterContent.length());
        Log.d("ReaderActivity", "User ID: " + currentUserId);
        Log.d("ReaderActivity", "Unique ID: " + currentChapterIndex);
        
        // Clear cache to ensure we get fresh TTS service with updated timeout
        RetrofitClient.clearCache();
        
        // Call TTS API
        com.example.LearnMate.network.api.TTSService ttsService = RetrofitClient.getTTSService(this);
        Call<List<com.example.LearnMate.network.dto.TTSResponse>> call = ttsService.convertTextToSpeech(request);
        
        Log.d("ReaderActivity", "TTS API call created with 5-minute timeout, sending request...");
        
        call.enqueue(new Callback<List<com.example.LearnMate.network.dto.TTSResponse>>() {
            @Override
            public void onResponse(Call<List<com.example.LearnMate.network.dto.TTSResponse>> call, 
                                 Response<List<com.example.LearnMate.network.dto.TTSResponse>> response) {
                Log.d("ReaderActivity", "=== TTS Response ===");
                Log.d("ReaderActivity", "Response code: " + response.code());
                Log.d("ReaderActivity", "Response successful: " + response.isSuccessful());
                
                // Hide loading
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                // Re-enable sound button
                if (btnSound != null) {
                    btnSound.setEnabled(true);
                }
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    com.example.LearnMate.network.dto.TTSResponse ttsResponse = response.body().get(0);
                    
                    if (ttsResponse.isCompleted() && ttsResponse.getResult() != null) {
                        // Play the audio from URL
                        playAudioFromUrl(ttsResponse.getResult());
                    } else {
                        Toast.makeText(ReaderActivity.this, 
                            "Audio not ready: " + ttsResponse.getStatus(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReaderActivity.this, 
                        "Failed to convert text to speech", 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<com.example.LearnMate.network.dto.TTSResponse>> call, Throwable t) {
                Log.e("ReaderActivity", "=== TTS Failure ===");
                Log.e("ReaderActivity", "Error: " + t.getMessage(), t);
                
                // Hide loading
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                // Re-enable sound button
                if (btnSound != null) {
                    btnSound.setEnabled(true);
                }
                
                Toast.makeText(ReaderActivity.this, 
                    "Error: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Play audio from URL using MediaPlayer
     */
    private void playAudioFromUrl(String audioUrl) {
        try {
            // Stop any existing audio
            stopAudio();
            
            // Create and prepare MediaPlayer
            mediaPlayer = new android.media.MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build()
            );
            
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlayingAudio = true;
                updateSoundIcon();
                Toast.makeText(ReaderActivity.this, "Playing audio...", Toast.LENGTH_SHORT).show();
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlayingAudio = false;
                updateSoundIcon();
                Toast.makeText(ReaderActivity.this, "Audio completed", Toast.LENGTH_SHORT).show();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("ReaderActivity", "MediaPlayer error: " + what + ", " + extra);
                isPlayingAudio = false;
                updateSoundIcon();
                Toast.makeText(ReaderActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                return true;
            });
            
            mediaPlayer.prepareAsync();
            
        } catch (Exception e) {
            Log.e("ReaderActivity", "Error playing audio", e);
            Toast.makeText(this, "Failed to play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Stop audio playback
     */
    private void stopAudio() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("ReaderActivity", "Error stopping audio", e);
            }
            mediaPlayer = null;
        }
        isPlayingAudio = false;
        updateSoundIcon();
    }
    
    /**
     * Update sound button icon based on playing state
     */
    private void updateSoundIcon() {
        if (btnSound != null) {
            if (isPlayingAudio) {
                btnSound.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                btnSound.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up MediaPlayer
        stopAudio();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Pause audio when activity is paused
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}
