package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.BookService;
import com.example.LearnMate.network.dto.BookChapterResponse;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookChapterListActivity extends AppCompatActivity {

    private RecyclerView rvChapters;
    private ChapterAdapter adapter;
    private CircularProgressIndicator progress;
    private TextView tvBookTitle;
    private String bookId;
    private String bookTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        // Get book info from intent
        bookId = getIntent().getStringExtra("book_id");
        bookTitle = getIntent().getStringExtra("book_title");

        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvBookTitle = findViewById(R.id.tvBook);
        rvChapters = findViewById(R.id.rvChapters);
        progress = findViewById(R.id.progress);
        
        // Set book title
        if (tvBookTitle != null) {
            tvBookTitle.setText(bookTitle != null ? bookTitle : "Đang tải...");
        }

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup RecyclerView
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new ChapterAdapter(new ArrayList<>());
        rvChapters.setAdapter(adapter);

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
                    List<BookChapterResponse> chapters = response.body();
                    android.util.Log.d("BookChapterListActivity", "Loaded " + chapters.size() + " chapters");
                    adapter.updateData(chapters);
                } else {
                    android.util.Log.e("BookChapterListActivity", "API call failed: " + response.code() + " - " + response.message());
                    Toast.makeText(BookChapterListActivity.this, "Không thể tải danh sách chương. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookChapterResponse>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("BookChapterListActivity", "API call error: " + t.getMessage(), t);
                Toast.makeText(BookChapterListActivity.this, "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progress != null) {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvChapters != null) {
            rvChapters.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // ===== Adapter =====
    class ChapterAdapter extends RecyclerView.Adapter<ChapterViewHolder> {
        private List<BookChapterResponse> chapters;

        ChapterAdapter(List<BookChapterResponse> chapters) {
            this.chapters = chapters != null ? chapters : new ArrayList<>();
        }

        public void updateData(List<BookChapterResponse> newChapters) {
            this.chapters = newChapters != null ? newChapters : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public ChapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chapter, parent, false);
            return new ChapterViewHolder(item);
        }

        @Override
        public void onBindViewHolder(ChapterViewHolder h, int position) {
            BookChapterResponse chapter = chapters.get(position);
            
            // Set title
            h.title.setText(chapter.title != null && !chapter.title.isEmpty() 
                    ? chapter.title 
                    : "Chapter " + (position + 1));

            // Set description (preview of content)
            String content = chapter.content != null ? chapter.content : "";
            String summary = content.length() > 120 
                    ? content.substring(0, 120) + "..." 
                    : content;
            // Remove line breaks
            summary = summary.replaceAll("\\n+", " ").trim();
            h.desc.setText(summary.isEmpty() ? "Không có nội dung" : summary);

            // Hide bookmark icon and menu button (can be added later if needed)
            if (h.bookmarkIcon != null) {
                h.bookmarkIcon.setVisibility(View.GONE);
            }
            if (h.menuButton != null) {
                h.menuButton.setVisibility(View.GONE);
            }

            // Click listener - open chapter content
            h.itemView.setOnClickListener(v -> {
                // Open BookReaderActivity with chapter content
                Intent intent = new Intent(BookChapterListActivity.this, BookReaderActivity.class);
                intent.putExtra("book_id", bookId);
                intent.putExtra("book_title", bookTitle);
                intent.putExtra("chapter_id", chapter.chapterId);
                intent.putExtra("chapter_title", chapter.title);
                intent.putExtra("chapter_content", chapter.content);
                intent.putExtra("chapter_index", position);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return chapters.size();
        }
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        android.widget.ImageView bookmarkIcon;
        android.widget.ImageButton menuButton;

        ChapterViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            desc = v.findViewById(R.id.tvDesc);
            bookmarkIcon = v.findViewById(R.id.ivBookmark);
            menuButton = v.findViewById(R.id.btnMenu);
        }
    }
}

