package com.example.LearnMate;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.network.OcrTranslateResponse;
import com.example.LearnMate.network.RetrofitUpload;
import com.example.LearnMate.reader.ChapterListActivity;
import com.example.LearnMate.reader.ChapterUtils;
import com.example.LearnMate.reader.ContentCache;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Import chỉ PDF + overlay Raw / Dịch */
public class ImportActivity extends AppCompatActivity {

    private RecyclerView rv;
    private PdfListAdapter adapter;
    private final List<PdfItem> items = new ArrayList<>();

    private MaterialCardView cardFileImport;

    private ActivityResultLauncher<String[]> openPdfLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Recycler
        rv = findViewById(R.id.rvImported);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PdfListAdapter(items);
        rv.setAdapter(adapter);

        // Only "Import from File"
        cardFileImport = findViewById(R.id.cardFileImport);
        cardFileImport.setOnClickListener(v -> openPdfPicker());

        // Bottom nav (menu ids: nav_home, nav_search, nav_import, nav_ai_bot, nav_profile)
        BottomNavigationView bottom = findViewById(R.id.bottom_navigation);
        bottom.setSelectedItemId(R.id.nav_import);
        bottom.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_import) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else {
                // search / ai-bot: tuỳ bạn xử lý sau
                Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // ActivityResult launcher – chỉ PDF
        openPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    // persist permission để dùng sau
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    if (!isPdf(getContentResolver(), uri)) {
                        Toast.makeText(this, "Vui lòng chọn file PDF", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = getDisplayName(uri);
                    if (name == null) name = "Document.pdf";
                    String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                    PdfItem added = new PdfItem(uri.toString(), name, date);
                    items.add(0, added);
                    adapter.notifyItemInserted(0);
                    rv.scrollToPosition(0);

                    // gợi ý: hiển thị overlay ngay sau khi import (tuỳ UX)
                    // adapter.showOverlayFor(0);
                }
        );
    }

    private void openPdfPicker() {
        // Chỉ cho PDF
        openPdfLauncher.launch(new String[]{"application/pdf"});
    }

    // ====== Helpers ======
    private boolean isPdf(ContentResolver cr, Uri uri) {
        String type = cr.getType(uri);
        if (type != null) return "application/pdf".equalsIgnoreCase(type);
        // fallback by extension
        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        return "pdf".equalsIgnoreCase(ext);
    }

    private String getDisplayName(Uri uri) {
        String result = null;
        Cursor c = null;
        try {
            c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) {
                result = c.getString(0);
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
        if (result == null) {
            String path = uri.getLastPathSegment();
            if (path != null) {
                int idx = path.lastIndexOf('/');
                result = idx >= 0 ? path.substring(idx + 1) : path;
            }
        }
        return result;
    }

    /** Gọi n8n OCR/Translate + tách chapter + mở ChapterList */
    private void processPdfAndOpen(Uri uri, String displayName, boolean translated) {
        Toast.makeText(this, translated ? "Đang dịch..." : "Đang đọc OCR...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // copy Uri -> file tạm
                java.io.File f = RetrofitUpload.tempFileFromUri(this, uri);

                OcrTranslateResponse res = RetrofitUpload.api()
                        .uploadAndTranslate(
                                RetrofitUpload.asPart(f),
                                RetrofitUpload.asText("user-123"))
                        .execute().body();

                String rawText   = (res != null && res.markdown   != null) ? res.markdown   : "";
                String transText = (res != null && res.translated != null) ? res.translated : null;

                ContentCache.RAW   = ChapterUtils.splitByRegex(rawText);
                ContentCache.TRANS = (transText == null) ? null : ChapterUtils.splitByRegex(transText);


                runOnUiThread(() -> {
                    Intent i = new Intent(this, ChapterListActivity.class);
                    i.putExtra("book_title", displayName);
                    i.putExtra("start_mode", translated ? "translate" : "raw");
                    startActivity(i);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Import lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // ====== Model + Adapter ======

    static class PdfItem {
        final String uriString;
        final String displayName;
        final String prettyDate;
        PdfItem(String uriString, String displayName, String prettyDate) {
            this.uriString = uriString; this.displayName = displayName; this.prettyDate = prettyDate;
        }
        Uri uri() { return Uri.parse(uriString); }
    }

    class PdfListAdapter extends RecyclerView.Adapter<PdfVH> {
        private final List<PdfItem> data;
        PdfListAdapter(List<PdfItem> seed) { data = seed; }

        @NonNull @Override
        public PdfVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pdf_card, parent, false);
            return new PdfVH(item);
        }

        @Override
        public void onBindViewHolder(@NonNull PdfVH h, int position) {
            PdfItem item = data.get(position);
            h.name.setText(item.displayName);
            h.date.setText(item.prettyDate);

            // Tap – show overlay (Raw/Dịch)
            h.itemView.setOnClickListener(v -> toggleOverlay(h));
            h.itemView.setOnLongClickListener(v -> { toggleOverlay(h); return true; });

            h.btnRaw.setOnClickListener(v -> {
                h.overlay.setVisibility(View.GONE);
                processPdfAndOpen(item.uri(), item.displayName, false);
            });

            h.btnTranslated.setOnClickListener(v -> {
                h.overlay.setVisibility(View.GONE);
                processPdfAndOpen(item.uri(), item.displayName, true);
            });
        }

        @Override public int getItemCount() { return data == null ? 0 : data.size(); }

        void showOverlayFor(int index) {
            if (index < 0 || index >= getItemCount()) return;
            notifyItemChanged(index);
        }

        private void toggleOverlay(PdfVH h) {
            h.overlay.setVisibility(h.overlay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    static class PdfVH extends RecyclerView.ViewHolder {
        final TextView name, date;
        final View overlay, btnRaw, btnTranslated;
        PdfVH(@NonNull View v) {
            super(v);
            name          = v.findViewById(R.id.tvName);
            date          = v.findViewById(R.id.tvDate);
            overlay       = v.findViewById(R.id.overlayActions);
            btnRaw        = v.findViewById(R.id.btnRaw);
            btnTranslated = v.findViewById(R.id.btnTranslated);
        }
    }
}
