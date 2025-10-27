// app/src/main/java/com/example/LearnMate/ImportActivity.java
package com.example.LearnMate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // <== QUAN TRỌNG
import android.widget.ImageView;
import android.widget.TextView; // để set tên/ngày trên card
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AiService;
import com.example.LearnMate.network.dto.UploadResponse;
import com.example.LearnMate.reader.ChapterListActivity;
import com.example.LearnMate.reader.ContentCache;
import com.example.LearnMate.service.ChapterPollingService;
import com.example.LearnMate.util.FileUtils;
import com.example.LearnMate.util.PdfThumbnailGenerator;
import com.example.LearnMate.util.PdfAnalyzer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportActivity extends AppCompatActivity {

    // Model class để lưu PDF với thumbnail và phân tích
    static class PdfItem {
        Uri uri;
        Bitmap thumbnail;
        String displayName;
        PdfAnalyzer.AnalysisResult analysis;

        PdfItem(Uri uri, Bitmap thumbnail, String displayName) {
            this.uri = uri;
            this.thumbnail = thumbnail;
            this.displayName = displayName;
        }

        PdfItem(Uri uri, Bitmap thumbnail, String displayName, PdfAnalyzer.AnalysisResult analysis) {
            this.uri = uri;
            this.thumbnail = thumbnail;
            this.displayName = displayName;
            this.analysis = analysis;
        }
    }

    private final List<PdfItem> imported = new ArrayList<>();
    private SimplePdfAdapter adapter;
    private View loadingOverlay;

    private final ActivityResultLauncher<String> pickPdf = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null)
                    uploadPdf(uri, getCurrentUserId());
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Loading overlay
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Nút "Import from File"
        findViewById(R.id.cardFileImport).setOnClickListener(v -> pickPdf.launch("application/pdf"));

        // Recycler grid hiển thị file đã import
        RecyclerView rv = findViewById(R.id.rvImported);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new SimplePdfAdapter(imported);
        rv.setAdapter(adapter);

        // BottomNavigationView: dùng menu của bạn (menu_bottom_home.xml) có id
        // nav_import, nav_home, nav_ai_bot, nav_profile
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
            } else if (id == R.id.nav_ai_bot) {
                // TODO: mở Bot
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    /** Lấy UserId hiện tại từ session */
    private String getCurrentUserId() {
        // TODO: Lấy UserId từ session/login state
        // Hiện tại dùng tạm thời, sau này sẽ lấy từ SharedPreferences hoặc Session
        return "user_" + System.currentTimeMillis(); // Tạm thời dùng timestamp
    }

    /** Hiển thị loading overlay */
    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }

    /** Ẩn loading overlay */
    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    /** Upload PDF tới /api/Ai/upload (multipart: File + UserId) */
    private void uploadPdf(Uri uri, String userId) {
        // Hiển thị loader
        showLoading();

        try {
            // Log để debug
            android.util.Log.d("ImportActivity", "Starting upload for URI: " + uri.toString());
            android.util.Log.d("ImportActivity", "UserId: " + userId);

            // TÊN PART phải chính xác theo Swagger: "File"
            MultipartBody.Part filePart = FileUtils.uriToPdfPart(this, uri, "File");
            // Truyền text part "UserId"
            RequestBody userPart = FileUtils.textPart(userId);

            android.util.Log.d("ImportActivity", "File part created successfully, starting upload...");
            AiService svc = RetrofitClient.get().create(AiService.class);
            svc.uploadPdf(filePart, userPart).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, retrofit2.Response<UploadResponse> resp) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        UploadResponse response = resp.body();

                        // Kiểm tra nếu có dữ liệu content ngay lập tức
                        if (response.content != null && !response.content.isEmpty()) {
                            // API đã trả về dữ liệu ngay lập tức
                            ContentCache.setChaptersFromUploadResponse(response);
                            Toast.makeText(ImportActivity.this, "✅ Upload thành công và đã lấy được dữ liệu!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // API chưa xử lý xong, cần polling
                            Toast.makeText(ImportActivity.this, "Upload OK - Job ID: " + response.jobId,
                                    Toast.LENGTH_SHORT).show();

                            // Lưu jobId và URI
                            ContentCache.setCurrentJobId(response.jobId);
                            ContentCache.setLastPdfUri(uri);

                            // Khởi tạo cache rỗng - chỉ dùng dữ liệu từ API
                            ContentCache.initializeEmpty();

                            // Bắt đầu polling để lấy chapters khi processing hoàn tất
                            ChapterPollingService pollingService = new ChapterPollingService(ImportActivity.this);
                            pollingService.startPolling(response.jobId);
                        }

                        // Generate thumbnail trong background thread
                        generateAndAddPdfItem(uri);

                    } else {
                        hideLoading();
                        Toast.makeText(ImportActivity.this, "Upload lỗi: " + resp.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    hideLoading();
                    android.util.Log.e("ImportActivity", "Network error: " + t.getMessage(), t);
                    Toast.makeText(ImportActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            hideLoading();
            android.util.Log.e("ImportActivity", "File error: " + e.getMessage(), e);
            Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Generate thumbnail, phân tích PDF và thêm vào danh sách */
    private void generateAndAddPdfItem(Uri uri) {
        String displayName = FileUtils.getDisplayName(this, uri);

        // Step 1: Phân tích PDF trước
        PdfAnalyzer.analyzeAsync(this, uri, new PdfAnalyzer.AnalysisCallback() {
            @Override
            public void onAnalysisComplete(PdfAnalyzer.AnalysisResult analysis) {
                android.util.Log.d("ImportActivity", "PDF Analysis: " + analysis.toString());

                // Step 2: Generate thumbnail sau khi phân tích xong
                PdfThumbnailGenerator.generateThumbnailAsync(ImportActivity.this, uri,
                        new PdfThumbnailGenerator.ThumbnailCallback() {
                            @Override
                            public void onThumbnailGenerated(Bitmap bitmap) {
                                // Update UI trên main thread
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    hideLoading();

                                    // Sử dụng title từ analysis nếu có
                                    String finalName = (analysis.title != null && !analysis.title.isEmpty())
                                            ? analysis.title
                                            : displayName;

                                    PdfItem item = new PdfItem(uri, bitmap, finalName, analysis);
                                    imported.add(item);
                                    adapter.notifyItemInserted(imported.size() - 1);

                                    // Show analysis result
                                    String message = String.format(
                                            "📚 %s\n🏷️ Category: %s\n📄 %d pages\n🌍 Language: %s",
                                            finalName,
                                            analysis.suggestedCategory,
                                            analysis.totalPages,
                                            analysis.detectedLanguage.toUpperCase());
                                    Toast.makeText(ImportActivity.this, message, Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                // Nếu lỗi thumbnail, vẫn thêm vào với analysis
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    hideLoading();
                                    String finalName = (analysis.title != null && !analysis.title.isEmpty())
                                            ? analysis.title
                                            : displayName;
                                    PdfItem item = new PdfItem(uri, null, finalName, analysis);
                                    imported.add(item);
                                    adapter.notifyItemInserted(imported.size() - 1);
                                    android.util.Log.e("ImportActivity",
                                            "Failed to generate thumbnail: " + e.getMessage());
                                });
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("ImportActivity", "Failed to analyze PDF: " + e.getMessage());

                // Nếu lỗi phân tích, vẫn generate thumbnail
                PdfThumbnailGenerator.generateThumbnailAsync(ImportActivity.this, uri,
                        new PdfThumbnailGenerator.ThumbnailCallback() {
                            @Override
                            public void onThumbnailGenerated(Bitmap bitmap) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    hideLoading();
                                    PdfItem item = new PdfItem(uri, bitmap, displayName);
                                    imported.add(item);
                                    adapter.notifyItemInserted(imported.size() - 1);
                                });
                            }

                            @Override
                            public void onError(Exception e2) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    hideLoading();
                                    PdfItem item = new PdfItem(uri, null, displayName);
                                    imported.add(item);
                                    adapter.notifyItemInserted(imported.size() - 1);
                                });
                            }
                        });
            }
        });
    }

    /** ===== Adapter card PDF có overlay Raw/Dịch ===== */
    static class SimplePdfAdapter extends RecyclerView.Adapter<PdfVH> {
        private final List<PdfItem> data;

        SimplePdfAdapter(List<PdfItem> d) {
            data = d;
        }

        @Override
        public PdfVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_card, parent, false);
            return new PdfVH(v);
        }

        @Override
        public void onBindViewHolder(PdfVH h, int pos) {
            PdfItem item = data.get(pos);
            Uri uri = item.uri;
            String name = item.displayName;

            // Views
            ImageView ivThumb = h.itemView.findViewById(R.id.ivThumb);
            TextView tvName = h.itemView.findViewById(R.id.tvName);
            TextView tvCategory = h.itemView.findViewById(R.id.tvCategory);
            TextView tvDate = h.itemView.findViewById(R.id.tvDate);
            View overlay = h.itemView.findViewById(R.id.overlayActions);
            View btnRaw = h.itemView.findViewById(R.id.btnRaw);
            View btnTr = h.itemView.findViewById(R.id.btnTranslated);

            // Hiển thị thumbnail thực tế của PDF
            if (ivThumb != null && item.thumbnail != null) {
                ivThumb.setImageBitmap(item.thumbnail);
                ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else if (ivThumb != null) {
                // Fallback nếu không có thumbnail
                ivThumb.setImageResource(R.drawable.ic_picture_as_pdf_24);
                ivThumb.setScaleType(ImageView.ScaleType.CENTER);
            }

            if (tvName != null)
                tvName.setText(name);

            // Hiển thị category nếu có phân tích
            if (tvCategory != null && item.analysis != null && item.analysis.suggestedCategory != null) {
                tvCategory.setText("🏷️ " + item.analysis.suggestedCategory);
                tvCategory.setVisibility(View.VISIBLE);
            } else if (tvCategory != null) {
                tvCategory.setVisibility(View.GONE);
            }

            // Hiển thị thông tin pages nếu có
            if (tvDate != null && item.analysis != null) {
                String info = String.format("%d pages • %s",
                        item.analysis.totalPages,
                        item.analysis.detectedLanguage.toUpperCase());
                tvDate.setText(info);
            } else if (tvDate != null) {
                tvDate.setText("Today");
            }

            // click card -> hiển thị/ẩn overlay Raw/Dịch
            h.itemView.setOnClickListener(
                    v -> overlay.setVisibility(overlay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));

            // Raw / Translate -> đi tới danh sách Chapter
            View.OnClickListener go = v -> {
                Intent i = new Intent(v.getContext(), ChapterListActivity.class);
                i.putExtra("pdf_uri", uri.toString());
                i.putExtra("book_title", name);
                i.putExtra("mode", v.getId() == R.id.btnRaw ? "raw" : "translate");
                v.getContext().startActivity(i);
            };
            btnRaw.setOnClickListener(go);
            btnTr.setOnClickListener(go);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    static class PdfVH extends RecyclerView.ViewHolder {
        PdfVH(View v) {
            super(v);
        }
    }
}
