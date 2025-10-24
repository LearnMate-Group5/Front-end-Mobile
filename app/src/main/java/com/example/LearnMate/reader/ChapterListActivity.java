package com.example.LearnMate.reader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.R;

import java.util.List;

public class ChapterListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Adapter adapter;
    private String startMode; // "raw" | "translate"

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        String bookTitle = getIntent().getStringExtra("book_title");
        startMode = getIntent().getStringExtra("start_mode");

        TextView tvBook = findViewById(R.id.tvBook);
        tvBook.setText(bookTitle == null ? "TRACE" : bookTitle);

        // nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // danh sách chương — lấy từ ContentCache (đã fill ở ImportActivity)
        List<ChapterUtils.Chapter> chapters =
                "translate".equalsIgnoreCase(startMode) && ContentCache.TRANS != null
                        ? ContentCache.TRANS
                        : ContentCache.RAW;

        rv = findViewById(R.id.rvChapters);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new Adapter(chapters);
        rv.setAdapter(adapter);
    }

    // ===== Adapter =====
    class Adapter extends RecyclerView.Adapter<VH> {
        private final List<ChapterUtils.Chapter> data;
        Adapter(List<ChapterUtils.Chapter> d){ this.data = d; }

        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View item = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chapter, parent, false);
            return new VH(item);
        }

        @Override public void onBindViewHolder(VH h, int pos) {
            ChapterUtils.Chapter c = data.get(pos);
            h.title.setText(c.title == null || c.title.isEmpty() ? "Chapter " + (pos+1) : c.title);
            String summary = c.content.length() > 120 ? c.content.substring(0,120) + "..." : c.content;
            h.desc.setText(summary);

            h.itemView.setOnClickListener(v -> {
                // mở ReaderActivity ở chapter pos
                ReaderActivity.open(
                        v.getContext(),
                        ContentCache.getLastPdfUri(), // nếu bạn muốn giữ uri: set trong ContentCache khi import
                        pos,
                        startMode == null ? "raw" : startMode
                );
            });
        }

        @Override public int getItemCount(){ return data == null ? 0 : data.size(); }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, desc;
        VH(android.view.View v){
            super(v);
            title = v.findViewById(R.id.tvTitle);
            desc  = v.findViewById(R.id.tvDesc);
        }
    }
}
