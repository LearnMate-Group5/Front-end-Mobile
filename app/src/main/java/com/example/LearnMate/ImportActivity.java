// app/src/main/java/com/example/LearnMate/ImportActivity.java
package com.example.LearnMate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // <== QUAN TRỌNG
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

    private final List<Uri> imported = new ArrayList<>();
    private SimplePdfAdapter adapter;

    private final ActivityResultLauncher<String> pickPdf = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null)
                    uploadPdf(uri, getCurrentUserId());
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

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

    /** Upload PDF tới /api/Ai/upload (multipart: File + UserId) */
    private void uploadPdf(Uri uri, String userId) {
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

                        // Thêm vào lưới để người dùng ấn Raw/Dịch
                        imported.add(uri);
                        adapter.notifyItemInserted(imported.size() - 1);

                    } else {
                        Toast.makeText(ImportActivity.this, "Upload lỗi: " + resp.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    android.util.Log.e("ImportActivity", "Network error: " + t.getMessage(), t);
                    Toast.makeText(ImportActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ImportActivity", "File error: " + e.getMessage(), e);
            Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** ===== Adapter card PDF có overlay Raw/Dịch ===== */
    static class SimplePdfAdapter extends RecyclerView.Adapter<PdfVH> {
        private final List<Uri> data;

        SimplePdfAdapter(List<Uri> d) {
            data = d;
        }

        @Override
        public PdfVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_card, parent, false);
            return new PdfVH(v);
        }

        @Override
        public void onBindViewHolder(PdfVH h, int pos) {
            Uri uri = data.get(pos);
            String name = FileUtils.getDisplayName(h.itemView.getContext(), uri);

            TextView tvName = h.itemView.findViewById(R.id.tvName);
            TextView tvDate = h.itemView.findViewById(R.id.tvDate);
            View overlay = h.itemView.findViewById(R.id.overlayActions);
            View btnRaw = h.itemView.findViewById(R.id.btnRaw);
            View btnTr = h.itemView.findViewById(R.id.btnTranslated);

            if (tvName != null)
                tvName.setText(name);
            if (tvDate != null)
                tvDate.setText("Today");

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
