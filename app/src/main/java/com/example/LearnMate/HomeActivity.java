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
        tvGreeting     = findViewById(R.id.tvGreeting);
        rvRecommended  = findViewById(R.id.rvRecommended);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        progress       = findViewById(R.id.progress);

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

    @Override protected void onStart() {
        super.onStart();
        String name = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_name", "User");
        tvGreeting.setText("Good Morning, " + name);
    }


    @Override protected void onDestroy() {
        presenter.detach();
        super.onDestroy();
    }

    // ================== HomeContract.View ==================
    @Override public void showGreeting(String name) {
        tvGreeting.setText("Good Morning, " + name);
    }

    @Override public void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public void renderFeatured(List<Book> items) {
        // This view is now static, no longer need to render
    }

    @Override public void renderRecommended(List<Book> items) {
        // This view is now static, no longer need to render from presenter
    }

    @Override public void openBookDetail(Book book) {
        Toast.makeText(this, "Open: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: startActivity(...) tới màn chi tiết
    }

    @Override public void openImport() {
        Toast.makeText(this, "Import screen", Toast.LENGTH_SHORT).show();
        // TODO: startActivity(...) tới màn import
    }

    @Override public void showMessage(String msg) {
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
        public int getItemCount() { return data.size(); }
    }

    private class MockRecommendedVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, cat;
        MockRecommendedVH(View v) {
            super(v);
            cover = v.findViewById(R.id.ivSmallCover);
            title = v.findViewById(R.id.tvSmallTitle);
            cat   = v.findViewById(R.id.tvSmallCat);
        }
    }
}
