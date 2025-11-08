package com.example.LearnMate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.BookService;
import com.example.LearnMate.network.dto.BookChapterResponse;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

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
    private TextView tvContent;
    private TextView btnPrev;
    private TextView btnNext;
    private SeekBar sbFont;
    private CircularProgressIndicator progress;

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
        tvContent = findViewById(R.id.tvContent);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        sbFont = findViewById(R.id.sbFont);
        progress = findViewById(R.id.progress);

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
        if (tvContent != null) {
            tvContent.setVisibility(show ? View.GONE : View.VISIBLE);
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
        if (tvContent != null) {
            String content = chapter.content != null ? chapter.content : "Không có nội dung";
            tvContent.setText(content);
            tvContent.setTextSize(currentFontSize);
            tvContent.setTextIsSelectable(true);
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
}

