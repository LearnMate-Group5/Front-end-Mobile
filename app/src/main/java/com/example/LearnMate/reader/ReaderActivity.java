package com.example.LearnMate.reader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.R;

public class ReaderActivity extends AppCompatActivity {

    /** Helper mở Reader nhanh */
    public static void open(Context ctx, Uri pdfUri, int chapterIndex, String mode) {
        Intent i = new Intent(ctx, ReaderActivity.class);
        i.putExtra("pdf_uri", pdfUri.toString());
        i.putExtra("chapter_index", chapterIndex);
        i.putExtra("mode", mode); // "raw" | "translate"
        ctx.startActivity(i);
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        // TODO: đọc extras và render nội dung chương
        // String uri = getIntent().getStringExtra("pdf_uri");
        // int chapter = getIntent().getIntExtra("chapter_index", 0);
        // String mode = getIntent().getStringExtra("mode");
    }
}
