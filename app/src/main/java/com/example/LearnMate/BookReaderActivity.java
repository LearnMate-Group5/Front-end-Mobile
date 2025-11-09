package com.example.LearnMate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.BookService;
import com.example.LearnMate.network.dto.BookChapterResponse;
import com.example.LearnMate.util.MarkdownWithMathHelper;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookReaderActivity extends AppCompatActivity {

    private List<BookChapterResponse> chapters;
    private int currentChapterIndex = 0;
    private float currentFontSize = 16f;
    private String bookId;
    private String bookTitle;

    private TextView tvBookTitle;
    private TextView tvChapterTitle;
    private WebView webViewContent;
    private TextView btnPrev;
    private TextView btnNext;
    private SeekBar sbFont;
    private CircularProgressIndicator progress;
    private ImageButton btnSound;
    
    // Text-to-Speech
    private android.media.MediaPlayer mediaPlayer;
    private boolean isPlayingAudio = false;
    private SessionManager sessionManager;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        // Get data from intent
        bookId = getIntent().getStringExtra("book_id");
        bookTitle = getIntent().getStringExtra("book_title");
        currentChapterIndex = getIntent().getIntExtra("chapter_index", 0);

        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        webViewContent = findViewById(R.id.webViewContent);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        sbFont = findViewById(R.id.sbFont);
        progress = findViewById(R.id.progress);
        btnSound = findViewById(R.id.btnSound);
        
        // Setup WebView for MathJax rendering
        if (webViewContent != null) {
            MarkdownWithMathHelper.setupWebViewForMath(webViewContent);
        }
        
        // Initialize session manager for user ID
        sessionManager = new SessionManager(this);
        com.example.LearnMate.network.dto.LoginResponse.UserData userData = sessionManager.getUserData();
        if (userData != null && userData.getUserId() != null) {
            currentUserId = userData.getUserId();
        } else {
            currentUserId = UUID.randomUUID().toString();
        }

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Hide bookmark and more buttons (can be added later if needed)
        ImageButton btnBookmark = findViewById(R.id.btnBookmark);
        ImageButton btnMore = findViewById(R.id.btnMore);
        if (btnBookmark != null) {
            btnBookmark.setVisibility(View.GONE);
        }
        if (btnMore != null) {
            btnMore.setVisibility(View.GONE);
        }
        
        // Setup sound button for TTS
        setupSoundButton();

        // Set book title
        if (tvBookTitle != null && bookTitle != null) {
            tvBookTitle.setText(bookTitle);
        }

        // Setup font size seekbar
        if (sbFont != null) {
            sbFont.setMax(20); // 12-32px
            sbFont.setProgress((int) currentFontSize - 12);
            sbFont.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        currentFontSize = 12 + progress;
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

        // Load chapters from API
        loadChapters();
    }

    /**
     * Load danh sách chapters từ API
     */
    private void loadChapters() {
        showLoading(true);

        BookService bookService = RetrofitClient.getBookService(this);
        Call<List<BookChapterResponse>> call = bookService.getBookChapters(bookId);

        call.enqueue(new Callback<List<BookChapterResponse>>() {
            @Override
            public void onResponse(Call<List<BookChapterResponse>> call, Response<List<BookChapterResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    chapters = response.body();
                    android.util.Log.d("BookReaderActivity", "Loaded " + chapters.size() + " chapters");

                    // Ensure chapter index is valid
                    if (currentChapterIndex < 0 || currentChapterIndex >= chapters.size()) {
                        currentChapterIndex = 0;
                    }

                    // Setup navigation buttons
                    setupNavigationButtons();

                    // Display current chapter
                    displayChapter();
                } else {
                    android.util.Log.e("BookReaderActivity", "API call failed: " + response.code());
                    Toast.makeText(BookReaderActivity.this, "Không thể tải nội dung. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookChapterResponse>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("BookReaderActivity", "API call error: " + t.getMessage(), t);
                Toast.makeText(BookReaderActivity.this, "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progress != null) {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (webViewContent != null) {
            webViewContent.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setupNavigationButtons() {
        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                if (currentChapterIndex > 0) {
                    currentChapterIndex--;
                    displayChapter();
                } else {
                    Toast.makeText(this, "Đây là chapter đầu tiên", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (chapters != null && currentChapterIndex < chapters.size() - 1) {
                    currentChapterIndex++;
                    displayChapter();
                } else {
                    Toast.makeText(this, "Đây là chapter cuối cùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayChapter() {
        if (chapters == null || chapters.isEmpty() || currentChapterIndex < 0 || currentChapterIndex >= chapters.size()) {
            return;
        }

        BookChapterResponse chapter = chapters.get(currentChapterIndex);

        // Update chapter title
        if (tvChapterTitle != null) {
            tvChapterTitle.setText(chapter.title != null ? chapter.title : "Chapter " + (currentChapterIndex + 1));
        }

        // Update content
        if (webViewContent != null) {
            String content = chapter.content != null ? chapter.content : "Không có nội dung";
            // Render markdown with MathJax
            MarkdownWithMathHelper.renderMarkdownWithMath(webViewContent, content);
            // Apply font size after rendering
            webViewContent.postDelayed(() -> {
                applyFontSize(webViewContent, currentFontSize);
            }, 1000);
            webViewContent.postDelayed(() -> {
                applyFontSize(webViewContent, currentFontSize);
            }, 3000);
        }

        // Update navigation buttons state
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        if (btnPrev != null) {
            btnPrev.setEnabled(currentChapterIndex > 0);
            btnPrev.setAlpha(currentChapterIndex > 0 ? 1.0f : 0.5f);
        }

        if (btnNext != null && chapters != null) {
            boolean hasNext = currentChapterIndex < chapters.size() - 1;
            btnNext.setEnabled(hasNext);
            btnNext.setAlpha(hasNext ? 1.0f : 0.5f);
        }
    }
    
    /**
     * Setup sound button for Text-to-Speech
     */
    private void setupSoundButton() {
        if (btnSound == null) {
            return;
        }
        
        btnSound.setOnClickListener(v -> {
            if (isPlayingAudio) {
                stopAudio();
            } else {
                // Show loading immediately when button is clicked
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
        
        BookChapterResponse currentChapter = chapters.get(currentChapterIndex);
        String chapterContent = currentChapter.content;
        
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
        
        Log.d("BookReaderActivity", "=== TTS Request ===");
        Log.d("BookReaderActivity", "Text length: " + chapterContent.length());
        Log.d("BookReaderActivity", "User ID: " + currentUserId);
        Log.d("BookReaderActivity", "Unique ID: " + currentChapterIndex);
        
        // Clear cache to ensure we get fresh TTS service with updated timeout
        RetrofitClient.clearCache();
        
        // Call TTS API
        com.example.LearnMate.network.api.TTSService ttsService = RetrofitClient.getTTSService(this);
        Call<List<com.example.LearnMate.network.dto.TTSResponse>> call = ttsService.convertTextToSpeech(request);
        
        Log.d("BookReaderActivity", "TTS API call created with 5-minute timeout, sending request...");
        
        call.enqueue(new Callback<List<com.example.LearnMate.network.dto.TTSResponse>>() {
            @Override
            public void onResponse(Call<List<com.example.LearnMate.network.dto.TTSResponse>> call, 
                                 Response<List<com.example.LearnMate.network.dto.TTSResponse>> response) {
                Log.d("BookReaderActivity", "=== TTS Response ===");
                Log.d("BookReaderActivity", "Response code: " + response.code());
                Log.d("BookReaderActivity", "Response successful: " + response.isSuccessful());
                
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
                        Toast.makeText(BookReaderActivity.this, 
                            "Audio not ready: " + ttsResponse.getStatus(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookReaderActivity.this, 
                        "Failed to convert text to speech", 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<com.example.LearnMate.network.dto.TTSResponse>> call, Throwable t) {
                Log.e("BookReaderActivity", "=== TTS Failure ===");
                Log.e("BookReaderActivity", "Error: " + t.getMessage(), t);
                
                // Hide loading
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                // Re-enable sound button
                if (btnSound != null) {
                    btnSound.setEnabled(true);
                }
                
                Toast.makeText(BookReaderActivity.this, 
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
                Toast.makeText(BookReaderActivity.this, "Playing audio...", Toast.LENGTH_SHORT).show();
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlayingAudio = false;
                updateSoundIcon();
                Toast.makeText(BookReaderActivity.this, "Audio completed", Toast.LENGTH_SHORT).show();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("BookReaderActivity", "MediaPlayer error: " + what + ", " + extra);
                isPlayingAudio = false;
                updateSoundIcon();
                Toast.makeText(BookReaderActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                return true;
            });
            
            mediaPlayer.prepareAsync();
            
        } catch (Exception e) {
            Log.e("BookReaderActivity", "Error playing audio", e);
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
                Log.e("BookReaderActivity", "Error stopping audio", e);
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
}

