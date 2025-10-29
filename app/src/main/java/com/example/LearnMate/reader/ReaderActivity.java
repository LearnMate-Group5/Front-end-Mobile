package com.example.LearnMate.reader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.R;

import java.util.List;

public class ReaderActivity extends AppCompatActivity {

    // Biến instance để lưu trạng thái
    private List<ChapterUtils.Chapter> chapters;
    private int currentChapterIndex = 0;
    private String currentMode = "raw";
    private float currentFontSize = 16f; // Font size mặc định

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
            if ("translate".equals(currentMode)) {
                // Hiển thị nội dung đã dịch
                tvContent.setText(currentChapter.translatedContent);
            } else {
                // Hiển thị nội dung gốc
                tvContent.setText(currentChapter.content);
            }
            tvContent.setTextSize(currentFontSize);
        }
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

    private void highlightCurrentText() {
        // TODO: Implement text highlighting functionality
        Toast.makeText(this, "Highlight feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void addNote() {
        // TODO: Implement note adding functionality
        Toast.makeText(this, "Note feature coming soon", Toast.LENGTH_SHORT).show();
    }
}
