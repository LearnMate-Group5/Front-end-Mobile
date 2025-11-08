package com.example.LearnMate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.BookService;
import com.example.LearnMate.network.dto.BookResponse;
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
    
    private BookService bookService;
    private List<BookResponse> allBooks;
    private List<BookResponse> filteredBooks;
    private SearchResultsAdapter searchAdapter;
    
    private String currentSearchQuery = "";
    private String selectedCategory = null;
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
        if (recyclerViewSearchResults != null) {
            recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewSearchResults.setAdapter(searchAdapter);
        }
        
        // Get references to popular topics views to show/hide
        View tvPopularTopics = findViewById(R.id.tvPopularTopics);
        View cardPopularTopics = findViewById(R.id.cardPopularTopics);

        // Initialize service
        bookService = RetrofitClient.getBookService(this);
        
        // Initialize data
        allBooks = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        searchAdapter = new SearchResultsAdapter(filteredBooks);
        
        // Load all books
        loadBooks();

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
            // Toggle filter
            if (btnFilterCategory.isSelected()) {
                btnFilterCategory.setSelected(false);
                selectedCategory = null;
            } else {
                btnFilterCategory.setSelected(true);
                // Show category selection dialog or dropdown
                Toast.makeText(this, "Chọn danh mục", Toast.LENGTH_SHORT).show();
            }
            performSearch();
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
    
    private void loadBooks() {
        bookService.getBooks().enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(Call<List<BookResponse>> call, Response<List<BookResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBooks = response.body();
                    performSearch();
                } else {
                    Toast.makeText(SearchActivity.this, "Không thể tải danh sách sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookResponse>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void performSearch() {
        filteredBooks.clear();
        
        // Show/hide search results and popular topics
        View tvPopularTopics = findViewById(R.id.tvPopularTopics);
        View cardPopularTopics = findViewById(R.id.cardPopularTopics);
        View recentSearches = findViewById(R.id.recentSearches);
        
        boolean hasQuery = !currentSearchQuery.isEmpty() || selectedCategory != null || selectedAuthor != null;
        
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
        
        for (BookResponse book : allBooks) {
            boolean matches = true;
            
            // Search by query
            if (!currentSearchQuery.isEmpty()) {
                String query = currentSearchQuery.toLowerCase();
                boolean matchesQuery = (book.title != null && book.title.toLowerCase().contains(query)) ||
                                      (book.author != null && book.author.toLowerCase().contains(query)) ||
                                      (book.description != null && book.description.toLowerCase().contains(query));
                
                // Also search in categories
                if (!matchesQuery && book.categories != null) {
                    for (String category : book.categories) {
                        if (category != null && category.toLowerCase().contains(query)) {
                            matchesQuery = true;
                            break;
                        }
                    }
                }
                
                if (!matchesQuery) {
                    matches = false;
                }
            }
            
            // Filter by category
            if (selectedCategory != null && book.categories != null && !book.categories.isEmpty()) {
                // Check if any category in the list matches
                boolean categoryMatches = false;
                for (String category : book.categories) {
                    if (category != null && category.equals(selectedCategory)) {
                        categoryMatches = true;
                        break;
                    }
                }
                if (!categoryMatches) {
                    matches = false;
                }
            }
            
            // Filter by author
            if (selectedAuthor != null && book.author != null) {
                if (!book.author.equals(selectedAuthor)) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredBooks.add(book);
            }
        }
        
        searchAdapter.notifyDataSetChanged();
    }
    
    // Simple adapter for search results
    private static class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        private List<BookResponse> books;
        
        SearchResultsAdapter(List<BookResponse> books) {
            this.books = books;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            // Create view holder - you may want to create a proper layout for this
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BookResponse book = books.get(position);
            holder.text1.setText(book.title != null ? book.title : "No Title");
            holder.text2.setText(book.author != null ? book.author : "Unknown Author");
        }
        
        @Override
        public int getItemCount() {
            return books.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView text1, text2;
            
            ViewHolder(android.view.View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
