package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.model.Book;
import com.example.LearnMate.model.HomeRepositoryImpl;
<<<<<<< Updated upstream
=======
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AiService;
import com.example.LearnMate.network.dto.AiFileResponse;

import java.util.List;
>>>>>>> Stashed changes
import com.example.LearnMate.presenter.HomeContract;
import com.example.LearnMate.presenter.HomePresenter;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.example.LearnMate.components.BottomNavigationComponent;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeContract.View {

    private TextView tvGreeting;
    private RecyclerView rvRecommended;
    private CircularProgressIndicator progress;
    private BottomNavigationComponent bottomNavComponent;

    private HomeContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ---- bind views ----
        tvGreeting = findViewById(R.id.tvGreeting);
        rvRecommended = findViewById(R.id.rvRecommended);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        progress = findViewById(R.id.progress);

        // ---- adapters & recyclers ----
        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommended.setAdapter(new MockRecommendedAdapter());

        findViewById(R.id.cardImport).setOnClickListener(v -> presenter.onImportClick());

        // ---- Bottom nav ----
        bottomNavComponent.setSelectedItem(R.id.nav_home);
        // Navigation is now handled automatically by BottomNavigationComponent

        // ---- presenter ----
        presenter = new HomePresenter(new HomeRepositoryImpl());
        presenter.attach(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String name = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_name", "User");
        tvGreeting.setText("Good Morning, " + name);
<<<<<<< Updated upstream
=======

        // Reload imported files when activity starts
        loadImportedFiles();
    }

    /**
     * Load danh sách files đã import từ Local History + API
     */
    private void loadImportedFiles() {
        if (importedFilesAdapter == null)
            return;

        // Load từ local history trước (instant)
        List<FileHistoryManager.ImportedFile> localFiles = fileHistoryManager.getFiles();

        if (!localFiles.isEmpty()) {
            // Convert sang AiFileResponse format
            List<AiFileResponse> fileResponses = new ArrayList<>();
            for (FileHistoryManager.ImportedFile file : localFiles) {
                AiFileResponse response = new AiFileResponse();
                response.fileName = file.fileName;
                response.fileId = file.uri; // Dùng URI để load thumbnail
                response.category = file.category;
                response.language = file.language;
                response.totalPages = file.totalPages;
                response.uploadedAt = new java.util.Date(file.importedAt).toString();
                fileResponses.add(response);
            }

            android.util.Log.d("HomeActivity", "Loaded " + fileResponses.size() + " files from local history");
            importedFilesAdapter.updateData(fileResponses);
        }

        // Optional: Sync với API ở background (nếu backend ready)
        // loadFromApi();
    }

    /** Optional: Load từ API để sync */
    private void loadFromApi() {
        AiService service = RetrofitClient.getRetrofitWithAuth(this).create(AiService.class);
        service.getFiles().enqueue(new Callback<List<AiFileResponse>>() {
            @Override
            public void onResponse(Call<List<AiFileResponse>> call, Response<List<AiFileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AiFileResponse> files = response.body();
                    if (files != null && !files.isEmpty()) {
                        android.util.Log.d("HomeActivity", "Synced " + files.size() + " files from API");
                        // Có thể merge với local data
                        importedFilesAdapter.updateData(files);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AiFileResponse>> call, Throwable t) {
                android.util.Log.e("HomeActivity", "API sync failed: " + t.getMessage());
                // Không sao, vẫn có local data
            }
        });
>>>>>>> Stashed changes
    }

    @Override
    protected void onDestroy() {
        presenter.detach();
        super.onDestroy();
    }

    // ================== HomeContract.View ==================
    @Override
    public void showGreeting(String name) {
        tvGreeting.setText("Good Morning, " + name);
    }

    @Override
    public void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderFeatured(List<Book> items) {
        // This view is now static, no longer need to render
    }

    @Override
    public void renderRecommended(List<Book> items) {
        // This view is now static, no longer need to render from presenter
    }

    @Override
    public void openBookDetail(Book book) {
        Toast.makeText(this, "Open: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: startActivity(...) tới màn chi tiết
    }

    @Override
    public void openImport() {
        Intent intent = new Intent(this, ImportActivity.class);
        startActivity(intent);
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ================== Adapters for Mock Data ==================

    /** Adapter for static recommended books with mock data */
    private class MockRecommendedAdapter extends RecyclerView.Adapter<MockRecommendedVH> {
        private class MockBook {
            final int imageRes;
            final String title;
            final String category;

            MockBook(int imageRes, String title, String category) {
                this.imageRes = imageRes;
                this.title = title;
                this.category = category;
            }
        }

        private final List<MockBook> data = new ArrayList<>();

        MockRecommendedAdapter() {
            // Create static data inside the adapter
            data.add(new MockBook(R.drawable.mock1, "The Silent Patient", "Thriller"));
            data.add(new MockBook(R.drawable.mock2, "Educated: A Memoir", "Memoir"));
            data.add(new MockBook(R.drawable.mock3, "Where the Crawdads Sing", "Fiction"));
            data.add(new MockBook(R.drawable.mock4, "Atomic Habits", "Self-help"));
        }

        @Override
        public MockRecommendedVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recommended_book, parent, false);
            return new MockRecommendedVH(v);
        }

        @Override
        public void onBindViewHolder(MockRecommendedVH h, int position) {
            MockBook b = data.get(position);
            h.cover.setImageResource(b.imageRes);
            h.title.setText(b.title);
            h.cat.setText(b.category);
            h.itemView.setOnClickListener(v -> {
                Toast.makeText(h.itemView.getContext(), "Open: " + b.title, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class MockRecommendedVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, cat;

        MockRecommendedVH(View v) {
            super(v);
            cover = v.findViewById(R.id.ivSmallCover);
            title = v.findViewById(R.id.tvSmallTitle);
            cat = v.findViewById(R.id.tvSmallCat);
        }
    }
}
