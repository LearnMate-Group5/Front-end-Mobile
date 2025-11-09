package com.example.LearnMate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private Chip btnFilterCategory;
    private Chip btnFilterAuthor;
    private Chip btnFilterRating;
    private View btnSearchSettings;
    private BottomNavigationComponent bottomNavComponent;
    private RecyclerView recyclerViewSearchResults;
    private ProgressBar progressBar;
    
    private BookService bookService;
    private List<BookResponse> searchResults;
    private List<CategoryResponse> categories;
    private BookSearchAdapter searchAdapter;
    
    private String currentSearchQuery = "";
    private String selectedCategoryId = null;
    private String selectedCategoryName = null;
    private String selectedAuthor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        btnFilterCategory = findViewById(R.id.btnFilterCategory);
        btnFilterAuthor = findViewById(R.id.btnFilterAuthor);
        btnFilterRating = findViewById(R.id.btnFilterRating);
        btnSearchSettings = findViewById(R.id.btnSearchSettings);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        
        // Initialize RecyclerView for search results
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize service
        bookService = RetrofitClient.getBookService(this);
        
        // Initialize data
        searchResults = new ArrayList<>();
        categories = new ArrayList<>();
        searchAdapter = new BookSearchAdapter(searchResults, book -> {
            // Handle book click - navigate to book detail or chapter list
            Toast.makeText(this, "Clicked: " + (book.title != null ? book.title : "Unknown"), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to book detail activity
        });
        
        if (recyclerViewSearchResults != null) {
            recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewSearchResults.setAdapter(searchAdapter);
        }
        
        // Load categories for filter
        loadCategories();

        // Search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter chips click listeners
        btnFilterCategory.setOnClickListener(v -> {
            // Show category selection dialog
            showCategorySelectionDialog();
        });

        btnFilterAuthor.setOnClickListener(v -> {
            if (btnFilterAuthor.isSelected()) {
                btnFilterAuthor.setSelected(false);
                selectedAuthor = null;
            } else {
                btnFilterAuthor.setSelected(true);
                Toast.makeText(this, "Chọn tác giả", Toast.LENGTH_SHORT).show();
            }
            performSearch();
        });

        btnFilterRating.setOnClickListener(v -> {
            Toast.makeText(this, "Lọc theo Đánh Giá", Toast.LENGTH_SHORT).show();
            performSearch();
        });

        btnSearchSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Cài Đặt Tìm Kiếm", Toast.LENGTH_SHORT).show();
        });

        // Setup bottom navigation
        bottomNavComponent.setSelectedItem(R.id.nav_search);
    }
    
    private void loadCategories() {
        bookService.getCategories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                // Silently fail - categories are optional
            }
        });
    }
    
    private void showCategorySelectionDialog() {
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục...", Toast.LENGTH_SHORT).show();
            loadCategories();
            return;
        }
        
        String[] categoryNames = new String[categories.size() + 1];
        categoryNames[0] = "Tất cả danh mục";
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i + 1] = categories.get(i).name != null ? categories.get(i).name : "Unknown";
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Chọn danh mục")
            .setItems(categoryNames, (dialog, which) -> {
                if (which == 0) {
                    // Clear category filter
                    selectedCategoryId = null;
                    selectedCategoryName = null;
                    btnFilterCategory.setSelected(false);
                } else {
                    CategoryResponse category = categories.get(which - 1);
                    selectedCategoryId = category.categoryId;
                    selectedCategoryName = category.name;
                    btnFilterCategory.setSelected(true);
                    btnFilterCategory.setText(category.name);
                }
                performSearch();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void performSearch() {
        // Show/hide search results and popular topics
        View tvPopularTopics = findViewById(R.id.tvPopularTopics);
        View cardPopularTopics = findViewById(R.id.cardPopularTopics);
        View recentSearches = findViewById(R.id.recentSearches);
        
        boolean hasQuery = !currentSearchQuery.isEmpty() || selectedCategoryId != null || selectedAuthor != null;
        
        if (recyclerViewSearchResults != null) {
            recyclerViewSearchResults.setVisibility(hasQuery ? View.VISIBLE : View.GONE);
        }
        if (tvPopularTopics != null) {
            tvPopularTopics.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
        }
        if (cardPopularTopics != null) {
            cardPopularTopics.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
        }
        if (recentSearches != null) {
            recentSearches.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
        }
        
        // If no query and no filter, don't search
        if (!hasQuery) {
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Call API with filters
        Call<List<BookResponse>> call;
        
        if (selectedCategoryId != null) {
            // Search by categoryId
            call = bookService.getBooks(true, selectedCategoryId, null);
        } else if (selectedCategoryName != null) {
            // Search by categoryName
            call = bookService.getBooks(true, null, selectedCategoryName);
        } else {
            // Search all active books
            call = bookService.getBooks(true, null, null);
        }
        
        call.enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    
                    // Filter by search query if provided
                    if (!currentSearchQuery.isEmpty()) {
                        String query = currentSearchQuery.toLowerCase();
                        for (BookResponse book : response.body()) {
                            boolean matches = false;
                            
                            // Search in title
                            if (book.title != null && book.title.toLowerCase().contains(query)) {
                                matches = true;
                            }
                            // Search in author
                            else if (book.author != null && book.author.toLowerCase().contains(query)) {
                                matches = true;
                            }
                            // Search in description
                            else if (book.description != null && book.description.toLowerCase().contains(query)) {
                                matches = true;
                            }
                            // Search in categories
                            else if (book.categories != null) {
                                for (String category : book.categories) {
                                    if (category != null && category.toLowerCase().contains(query)) {
                                        matches = true;
                                        break;
                                    }
                                }
                            }
                            
                            if (matches) {
                                searchResults.add(book);
                            }
                        }
                    } else {
                        // No search query, show all results from API
                        searchResults.addAll(response.body());
                    }
                    
                    searchAdapter.notifyDataSetChanged();
                    
                    if (searchResults.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Không thể tải kết quả tìm kiếm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(SearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
