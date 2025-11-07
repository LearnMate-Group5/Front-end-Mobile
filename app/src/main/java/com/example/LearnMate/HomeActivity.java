package com.example.LearnMate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.managers.FavoriteBookManager;
import com.example.LearnMate.model.Book;
import com.example.LearnMate.model.HomeRepositoryImpl;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.BookService;
import com.example.LearnMate.network.dto.BookResponse;
import com.example.LearnMate.presenter.HomeContract;
import com.example.LearnMate.presenter.HomePresenter;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.example.LearnMate.components.BottomNavigationComponent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements HomeContract.View {

    private TextView tvGreeting;
    private RecyclerView rvFeaturedBooks;
    private RecyclerView rvRecommended;
    private RecyclerView rvFavoriteBooks;
    private CircularProgressIndicator progress;
    private BottomNavigationComponent bottomNavComponent;

    private HomeContract.Presenter presenter;
    private FeaturedBooksAdapter featuredBooksAdapter;
    private RecommendedBooksAdapter recommendedBooksAdapter;
    private FavoriteBooksAdapter favoriteBooksAdapter;
    private FavoriteBookManager favoriteBookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ---- bind views ----
        tvGreeting = findViewById(R.id.tvGreeting);
        rvFeaturedBooks = findViewById(R.id.rvFeaturedBooks);
        rvRecommended = findViewById(R.id.rvRecommended);
        rvFavoriteBooks = findViewById(R.id.rvFavoriteBooks);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        progress = findViewById(R.id.progress);

        // ---- initialize managers ----
        favoriteBookManager = new FavoriteBookManager(this);

        // ---- adapters & recyclers ----
        rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredBooksAdapter = new FeaturedBooksAdapter(new ArrayList<>());
        rvFeaturedBooks.setAdapter(featuredBooksAdapter);

        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedBooksAdapter = new RecommendedBooksAdapter(new ArrayList<>());
        rvRecommended.setAdapter(recommendedBooksAdapter);

        rvFavoriteBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        favoriteBooksAdapter = new FavoriteBooksAdapter(new ArrayList<>());
        rvFavoriteBooks.setAdapter(favoriteBooksAdapter);

        findViewById(R.id.cardImport).setOnClickListener(v -> presenter.onImportClick());

        // ---- Bottom nav ----
        bottomNavComponent.setSelectedItem(R.id.nav_home);
        // Navigation is now handled automatically by BottomNavigationComponent

        // ---- presenter ----
        presenter = new HomePresenter(new HomeRepositoryImpl());
        presenter.attach(this);
        
        // Load subscription nếu đã login (để cache sẵn cho SettingsActivity)
        com.example.LearnMate.managers.SessionManager sessionManager = new com.example.LearnMate.managers.SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            com.example.LearnMate.managers.SubscriptionManager.getInstance(this).loadSubscriptionFromAPI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        String name = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("user_name", "User");
        tvGreeting.setText("Chào buổi sáng, " + name);
        
        // Load books from API
        loadRecommendedBooks();
        
        // Load favorite books
        loadFavoriteBooks();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Reload favorite books when returning to activity
        loadFavoriteBooks();
        
        // Kiểm tra nếu có flag refresh_subscription từ PaymentSuccessActivity
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("refresh_subscription", false)) {
            // Refresh subscription khi quay lại từ PaymentSuccessActivity
            com.example.LearnMate.managers.SessionManager sessionManager = 
                new com.example.LearnMate.managers.SessionManager(this);
            if (sessionManager.isLoggedIn()) {
                com.example.LearnMate.managers.SubscriptionManager.getInstance(this).loadSubscriptionFromAPI();
            }
            // Xóa flag để không refresh lại lần sau
            intent.removeExtra("refresh_subscription");
        }
    }
    
    /**
     * Load danh sách books từ API GET /api/Book
     */
    private void loadRecommendedBooks() {
        showLoading(true);
        
        BookService bookService = RetrofitClient.getBookService(this);
        Call<List<BookResponse>> call = bookService.getBooks();
        
        call.enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<BookResponse> books = response.body();
                    android.util.Log.d("HomeActivity", "Loaded " + books.size() + " books from API");
                    
                    // Sort books by likeCount descending for Featured Books
                    List<BookResponse> sortedBooks = new ArrayList<>(books);
                    sortedBooks.sort((b1, b2) -> Integer.compare(b2.likeCount, b1.likeCount));
                    
                    // Update Featured Books (sorted by likeCount)
                    featuredBooksAdapter.updateData(sortedBooks);
                    
                    // Update Recommended Books (all books, can be shuffled or filtered)
                    recommendedBooksAdapter.updateData(books);
                } else {
                    android.util.Log.e("HomeActivity", "API call failed: " + response.code() + " - " + response.message());
                    showMessage("Không thể tải danh sách sách. Vui lòng thử lại.");
                }
            }
            
            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("HomeActivity", "API call error: " + t.getMessage(), t);
                showMessage("Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.");
            }
        });
    }
    
    /**
     * Load danh sách sách yêu thích từ local storage
     */
    private void loadFavoriteBooks() {
        List<BookResponse> favorites = favoriteBookManager.getFavorites();
        favoriteBooksAdapter.updateData(favorites);
        android.util.Log.d("HomeActivity", "Loaded " + favorites.size() + " favorite books");
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Kiểm tra nếu có flag refresh_subscription từ PaymentSuccessActivity
        if (intent != null && intent.getBooleanExtra("refresh_subscription", false)) {
            // Refresh subscription khi quay lại từ PaymentSuccessActivity
            com.example.LearnMate.managers.SessionManager sessionManager = 
                new com.example.LearnMate.managers.SessionManager(this);
            if (sessionManager.isLoggedIn()) {
                com.example.LearnMate.managers.SubscriptionManager.getInstance(this).loadSubscriptionFromAPI();
            }
        }
    }
    


    @Override
    protected void onDestroy() {
        presenter.detach();
        super.onDestroy();
    }

    // ================== HomeContract.View ==================
    @Override
    public void showGreeting(String name) {
        tvGreeting.setText("Chào buổi sáng, " + name);
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
        Toast.makeText(this, "Mở: " + book.getTitle(), Toast.LENGTH_SHORT).show();
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

    // ================== Adapters for API Data ==================

    /** Adapter for featured books (sorted by likeCount descending) */
    private class FeaturedBooksAdapter extends RecyclerView.Adapter<FeaturedBookVH> {
        private List<BookResponse> books;

        FeaturedBooksAdapter(List<BookResponse> books) {
            this.books = books != null ? books : new ArrayList<>();
        }

        public void updateData(List<BookResponse> newBooks) {
            this.books = newBooks != null ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public FeaturedBookVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_featured_book, parent, false);
            return new FeaturedBookVH(v);
        }

        @Override
        public void onBindViewHolder(FeaturedBookVH h, int position) {
            BookResponse book = books.get(position);
            
            // Set title
            if (h.title != null) {
                h.title.setText(book.title != null ? book.title : "Không có tiêu đề");
            }
            
            // Set like count
            if (h.likeCount != null) {
                h.likeCount.setText(String.valueOf(book.likeCount));
            }
            
            // Load image from base64
            if (book.imageBase64 != null && !book.imageBase64.isEmpty()) {
                Bitmap bitmap = decodeBase64ToBitmap(book.imageBase64);
                if (bitmap != null) {
                    h.cover.setImageBitmap(bitmap);
                    h.cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    h.cover.setImageResource(R.drawable.bg_placeholder);
                }
            } else {
                h.cover.setImageResource(R.drawable.bg_placeholder);
            }
            
            // Setup favorite button
            ImageButton btnFavorite = h.itemView.findViewById(R.id.btnFavorite);
            if (btnFavorite != null) {
                boolean isFavorite = favoriteBookManager.isFavorite(book.bookId);
                updateFavoriteButton(btnFavorite, isFavorite);
                
                btnFavorite.setOnClickListener(v -> {
                    v.setEnabled(false);
                    boolean wasAdded = favoriteBookManager.toggleFavorite(book);
                    updateFavoriteButton(btnFavorite, wasAdded);
                    loadFavoriteBooks();
                    featuredBooksAdapter.notifyDataSetChanged();
                    recommendedBooksAdapter.notifyDataSetChanged();
                    String message = wasAdded ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
                    Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                });
                
                btnFavorite.setClickable(true);
                btnFavorite.setFocusable(true);
            }
            
            // Set click listener - navigate to BookChapterListActivity
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, BookChapterListActivity.class);
                intent.putExtra("book_id", book.bookId);
                intent.putExtra("book_title", book.title);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
    }

    private class FeaturedBookVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;
        TextView likeCount;

        FeaturedBookVH(View v) {
            super(v);
            cover = v.findViewById(R.id.ivFeaturedCover);
            title = v.findViewById(R.id.tvTitle);
            likeCount = v.findViewById(R.id.tvLikeCount);
        }
    }

    /** Adapter for recommended books from API */
    private class RecommendedBooksAdapter extends RecyclerView.Adapter<RecommendedBookVH> {
        private List<BookResponse> books;

        RecommendedBooksAdapter(List<BookResponse> books) {
            this.books = books != null ? books : new ArrayList<>();
        }

        public void updateData(List<BookResponse> newBooks) {
            this.books = newBooks != null ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public RecommendedBookVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recommended_book, parent, false);
            return new RecommendedBookVH(v);
        }

        @Override
        public void onBindViewHolder(RecommendedBookVH h, int position) {
            BookResponse book = books.get(position);
            
            // Set title
            h.title.setText(book.title != null ? book.title : "Không có tiêu đề");
            
            // Load image from base64
            if (book.imageBase64 != null && !book.imageBase64.isEmpty()) {
                Bitmap bitmap = decodeBase64ToBitmap(book.imageBase64);
                if (bitmap != null) {
                    h.cover.setImageBitmap(bitmap);
                    h.cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    // Fallback to placeholder if decode fails
                    h.cover.setImageResource(R.drawable.bg_placeholder);
                }
            } else {
                // Use placeholder if no image
                h.cover.setImageResource(R.drawable.bg_placeholder);
            }
            
            // Hide delete button (not needed for recommended books)
            ImageButton btnDelete = h.itemView.findViewById(R.id.btnDeleteSmall);
            if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
            }
            
            // Setup favorite button
            ImageButton btnFavorite = h.itemView.findViewById(R.id.btnFavorite);
            if (btnFavorite != null) {
                // Check if book is favorite
                boolean isFavorite = favoriteBookManager.isFavorite(book.bookId);
                updateFavoriteButton(btnFavorite, isFavorite);
                
                // Set click listener for favorite button
                btnFavorite.setOnClickListener(v -> {
                    v.setEnabled(false); // Prevent double click
                    boolean wasAdded = favoriteBookManager.toggleFavorite(book);
                    updateFavoriteButton(btnFavorite, wasAdded);
                    
                    // Refresh favorite books list
                    loadFavoriteBooks();
                    
                    // Refresh recommended list to update all favorite buttons
                    recommendedBooksAdapter.notifyDataSetChanged();
                    
                    // Show toast
                    String message = wasAdded ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
                    Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    v.setEnabled(true);
                });
                
                // Make button clickable and focusable to prevent click propagation
                btnFavorite.setClickable(true);
                btnFavorite.setFocusable(true);
            }
            
            // Set click listener - navigate to BookChapterListActivity
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, BookChapterListActivity.class);
                intent.putExtra("book_id", book.bookId);
                intent.putExtra("book_title", book.title);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
    }

    private class RecommendedBookVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;

        RecommendedBookVH(View v) {
            super(v);
            cover = v.findViewById(R.id.ivSmallCover);
            title = v.findViewById(R.id.tvSmallTitle);
        }
    }

    /**
     * Update favorite button icon based on favorite status
     */
    private void updateFavoriteButton(ImageButton btnFavorite, boolean isFavorite) {
        if (btnFavorite == null) return;
        
        if (isFavorite) {
            // Filled heart (red)
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            btnFavorite.setColorFilter(0xFFFF4444); // Red color
        } else {
            // Empty heart (white)
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            btnFavorite.setColorFilter(0xFFFFFFFF); // White color
        }
    }

    /**
     * Decode base64 string to Bitmap
     */
    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            // Remove data URL prefix if present (e.g., "data:image/png;base64,")
            String base64Image = base64String;
            if (base64String.contains(",")) {
                base64Image = base64String.substring(base64String.indexOf(",") + 1);
            }
            
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Error decoding base64 image: " + e.getMessage());
            return null;
        }
    }

    // ================== Favorite Books Adapter ==================

    /** Adapter for favorite books */
    private class FavoriteBooksAdapter extends RecyclerView.Adapter<FavoriteBookVH> {
        private List<BookResponse> books;

        FavoriteBooksAdapter(List<BookResponse> books) {
            this.books = books != null ? books : new ArrayList<>();
        }

        public void updateData(List<BookResponse> newBooks) {
            this.books = newBooks != null ? newBooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public FavoriteBookVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recommended_book, parent, false);
            return new FavoriteBookVH(v);
        }

        @Override
        public void onBindViewHolder(FavoriteBookVH h, int position) {
            BookResponse book = books.get(position);
            
            // Set title
            h.title.setText(book.title != null ? book.title : "Không có tiêu đề");
            
            // Load image from base64
            if (book.imageBase64 != null && !book.imageBase64.isEmpty()) {
                Bitmap bitmap = decodeBase64ToBitmap(book.imageBase64);
                if (bitmap != null) {
                    h.cover.setImageBitmap(bitmap);
                    h.cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    h.cover.setImageResource(R.drawable.bg_placeholder);
                }
            } else {
                h.cover.setImageResource(R.drawable.bg_placeholder);
            }
            
            // Hide delete button
            ImageButton btnDelete = h.itemView.findViewById(R.id.btnDeleteSmall);
            if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
            }
            
            // Setup favorite button (should always be filled for favorites)
            ImageButton btnFavorite = h.itemView.findViewById(R.id.btnFavorite);
            if (btnFavorite != null) {
                updateFavoriteButton(btnFavorite, true);
                
                btnFavorite.setOnClickListener(v -> {
                    favoriteBookManager.removeFavorite(book.bookId);
                    loadFavoriteBooks(); // Refresh list
                    recommendedBooksAdapter.notifyDataSetChanged(); // Update recommended list too
                    Toast.makeText(HomeActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
            }
            
            // Set click listener - navigate to BookChapterListActivity
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, BookChapterListActivity.class);
                intent.putExtra("book_id", book.bookId);
                intent.putExtra("book_title", book.title);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
    }

    private class FavoriteBookVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;

        FavoriteBookVH(View v) {
            super(v);
            cover = v.findViewById(R.id.ivSmallCover);
            title = v.findViewById(R.id.tvSmallTitle);
        }
    }

}
