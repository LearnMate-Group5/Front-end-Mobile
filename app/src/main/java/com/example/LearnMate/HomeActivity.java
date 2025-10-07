package com.example.LearnMate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeContract.View {

    private TextView tvGreeting;
    private RecyclerView rvFeatured, rvRecommended;
    private CircularProgressIndicator progress;
    private BottomNavigationView bottomNav;

    private FeaturedAdapter featuredAdapter;
    private RecommendedAdapter recommendedAdapter;

    private HomeContract.Presenter presenter;

    /** Interface click chung cho 2 adapter (đặt ngoài adapter để tránh lỗi static). */
    private interface OnBookClick { void onClick(Book b); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ---- bind views ----
        tvGreeting     = findViewById(R.id.tvGreeting);
        rvFeatured     = findViewById(R.id.rvFeatured);
        rvRecommended  = findViewById(R.id.rvRecommended);
        bottomNav      = findViewById(R.id.bottomNav);
        progress       = findViewById(R.id.progress);

        // ---- adapters & recyclers ----
        featuredAdapter = new FeaturedAdapter(book -> presenter.onFeaturedClick(book));
        rvFeatured.setLayoutManager(new GridLayoutManager(this, 2));
        rvFeatured.setAdapter(featuredAdapter);

        recommendedAdapter = new RecommendedAdapter(book -> presenter.onRecommendedClick(book));
        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommended.setAdapter(recommendedAdapter);

        findViewById(R.id.cardImport).setOnClickListener(v -> presenter.onImportClick());

        // ---- Bottom nav ----
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_import) {
                presenter.onImportClick();
                return true;
            }
            // các tab khác demo chưa làm
            return true;
        });

        // Né gesture bar / cutout: cộng padding đáy theo system insets
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets sb = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), sb.bottom);
            return insets;
        });

        // ---- presenter ----
        presenter = new HomePresenter(new HomeRepositoryImpl());
        presenter.attach(this);
    }

    @Override protected void onStart() {
        super.onStart();
        presenter.onStart();
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
        featuredAdapter.submit(items);
    }

    @Override public void renderRecommended(List<Book> items) {
        recommendedAdapter.submit(items);
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

    // ================== Adapters (inner classes, không static) ==================

    /** Featured: lưới 2 cột */
    private class FeaturedAdapter extends RecyclerView.Adapter<FeaturedVH> {
        private final List<Book> data = new ArrayList<>();
        private final OnBookClick onClick;

        FeaturedAdapter(OnBookClick c) { this.onClick = c; }

        void submit(List<Book> items) {
            data.clear();
            if (items != null) data.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public FeaturedVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_featured_book, parent, false);
            return new FeaturedVH(v);
        }

        @Override
        public void onBindViewHolder(FeaturedVH h, int position) {
            Book b = data.get(position);
            h.title.setText(b.getTitle());
            h.rating.setText(String.valueOf(b.getRating()));
            h.itemView.setOnClickListener(v -> onClick.onClick(b));
        }

        @Override
        public int getItemCount() { return data.size(); }
    }

    private class FeaturedVH extends RecyclerView.ViewHolder {
        TextView title, rating;
        FeaturedVH(View v) {
            super(v);
            title  = v.findViewById(R.id.tvTitle);
            rating = v.findViewById(R.id.tvRating);
        }
    }

    /** Recommended: danh sách ngang */
    private class RecommendedAdapter extends RecyclerView.Adapter<RecommendedVH> {
        private final List<Book> data = new ArrayList<>();
        private final OnBookClick onClick;

        RecommendedAdapter(OnBookClick c) { this.onClick = c; }

        void submit(List<Book> items) {
            data.clear();
            if (items != null) data.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public RecommendedVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recommended_book, parent, false);
            return new RecommendedVH(v);
        }

        @Override
        public void onBindViewHolder(RecommendedVH h, int position) {
            Book b = data.get(position);
            h.title.setText(b.getTitle());
            h.cat.setText(b.getCategory());
            h.itemView.setOnClickListener(v -> onClick.onClick(b));
        }

        @Override
        public int getItemCount() { return data.size(); }
    }

    private class RecommendedVH extends RecyclerView.ViewHolder {
        TextView title, cat;
        RecommendedVH(View v) {
            super(v);
            title = v.findViewById(R.id.tvSmallTitle);
            cat   = v.findViewById(R.id.tvSmallCat);
        }
    }
}
