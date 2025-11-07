package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.adapter.BookSearchAdapter;
import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.BookService;
import com.example.LearnMate.network.dto.BookResponse;
import com.example.LearnMate.network.dto.CategoryResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private Chip btnFilterCategory;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBarSearch;
    private TextView tvSearchResults;
    private TextView tvEmptyState;
    private View llRecentSearches;
    private ImageView btnSearchSettings;
    private BottomNavigationComponent bottomNavComponent;
    
    private BookService bookService;
    private BookSearchAdapter bookAdapter;
    private List<BookResponse> allBooks;
    private List<CategoryResponse> categories;
    private String selectedCategoryId;
    private String selectedCategoryName;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        btnFilterCategory = findViewById(R.id.btnFilterCategory);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        progressBarSearch = findViewById(R.id.progressBarSearch);
        tvSearchResults = findViewById(R.id.tvSearchResults);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        llRecentSearches = findViewById(R.id.llRecentSearches);
        btnSearchSettings = findViewById(R.id.btnSearchSettings);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Initialize BookService
        bookService = RetrofitClient.getBookService(this);

        // Setup RecyclerView
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookSearchAdapter(new ArrayList<>(), book -> {
            // Handle book click - navigate to book details or reader
            Toast.makeText(this, "Selected: " + book.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to book details activity
        });
        rvSearchResults.setAdapter(bookAdapter);

        // Load categories
        loadCategories();

        // Setup search text watcher with debounce
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Schedule new search after 500ms delay (debounce)
                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Category filter click
        btnFilterCategory.setOnClickListener(v -> showCategoryFilterDialog());

        // Search settings click
        btnSearchSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Cài Đặt Tìm Kiếm", Toast.LENGTH_SHORT).show();
        });

        // Setup bottom navigation
        bottomNavComponent.setSelectedItem(R.id.nav_search);
    }

    /**
     * Load all categories from API
     */
    private void loadCategories() {
        bookService.getCategories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                } else {
                    android.util.Log.e("SearchActivity", "Failed to load categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                android.util.Log.e("SearchActivity", "Error loading categories", t);
            }
        });
    }

    /**
     * Show category filter dialog
     */
    private void showCategoryFilterDialog() {
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục...", Toast.LENGTH_SHORT).show();
            loadCategories();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Danh Mục");

        // Create chip group for categories
        ChipGroup chipGroup = new ChipGroup(this);
        chipGroup.setPadding(32, 16, 32, 16);

        // Add "All" option
        Chip chipAll = new Chip(this);
        chipAll.setText("Tất Cả");
        chipAll.setCheckable(true);
        chipAll.setChecked(selectedCategoryId == null);
        chipAll.setOnClickListener(v -> {
            selectedCategoryId = null;
            selectedCategoryName = null;
            btnFilterCategory.setText("Danh Mục");
            performSearch(etSearch.getText().toString());
            builder.create().dismiss();
        });
        chipGroup.addView(chipAll);

        // Add category chips
        for (CategoryResponse category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            if (selectedCategoryId != null && selectedCategoryId.equals(category.getCategoryId().toString())) {
                chip.setChecked(true);
            }
            chip.setOnClickListener(v -> {
                selectedCategoryId = category.getCategoryId().toString();
                selectedCategoryName = category.getName();
                btnFilterCategory.setText(category.getName());
                performSearch(etSearch.getText().toString());
                builder.create().dismiss();
            });
            chipGroup.addView(chip);
        }

        builder.setView(chipGroup);
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Perform search with current filters
     */
    private void performSearch(String searchQuery) {
        String query = searchQuery != null ? searchQuery.trim() : "";
        
        // Show/hide UI elements
        if (query.isEmpty() && selectedCategoryId == null && selectedCategoryName == null) {
            // No search and no filter, show recent searches
            rvSearchResults.setVisibility(View.GONE);
            tvSearchResults.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Nhập từ khóa để tìm kiếm sách");
            llRecentSearches.setVisibility(View.VISIBLE);
            return;
        }

        // Hide recent searches, show search results
        llRecentSearches.setVisibility(View.GONE);
        tvSearchResults.setVisibility(View.VISIBLE);
        progressBarSearch.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        // Call API to search books
        Call<List<BookResponse>> call;
        if (selectedCategoryId != null) {
            // Search by category ID
            call = bookService.getBooks(true, selectedCategoryId, null);
        } else if (selectedCategoryName != null) {
            // Search by category name
            call = bookService.getBooks(true, null, selectedCategoryName);
        } else {
            // Get all active books
            call = bookService.getBooks(true, null, null);
        }

        call.enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                progressBarSearch.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<BookResponse> books = response.body();
                    if (books == null) {
                        books = new ArrayList<>();
                    }
                    
                    // Filter by search query if provided
                    if (!query.isEmpty()) {
                        books = filterBooksByQuery(books, query);
                    }
                    
                    // Update UI
                    if (books.isEmpty()) {
                        rvSearchResults.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Không tìm thấy kết quả nào");
                    } else {
                        rvSearchResults.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                        bookAdapter.updateBooks(books);
                        tvSearchResults.setText("Tìm thấy " + books.size() + " kết quả");
                    }
                } else {
                    rvSearchResults.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Lỗi khi tìm kiếm: " + response.code());
                    android.util.Log.e("SearchActivity", "Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                progressBarSearch.setVisibility(View.GONE);
                rvSearchResults.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Lỗi kết nối: " + t.getMessage());
                android.util.Log.e("SearchActivity", "Search error", t);
            }
        });
    }

    /**
     * Filter books by search query (title, author, description)
     */
    private List<BookResponse> filterBooksByQuery(List<BookResponse> books, String query) {
        List<BookResponse> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (BookResponse book : books) {
            // Search in title
            if (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerQuery)) {
                filtered.add(book);
                continue;
            }
            
            // Search in author
            if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowerQuery)) {
                filtered.add(book);
                continue;
            }
            
            // Search in description
            if (book.getDescription() != null && book.getDescription().toLowerCase().contains(lowerQuery)) {
                filtered.add(book);
                continue;
            }
            
            // Search in category names
            if (book.getCategories() != null) {
                for (String category : book.getCategories()) {
                    if (category != null && category.toLowerCase().contains(lowerQuery)) {
                        filtered.add(book);
                        break;
                    }
                }
            }
        }
        
        return filtered;
    }
}
