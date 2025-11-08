package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.BookChapterResponse;
import com.example.LearnMate.network.dto.BookResponse;
import com.example.LearnMate.network.dto.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Book Service API
 * Handles all book-related API calls
 */
public interface BookService {

    /**
     * Get all books with optional filters
     * API may return List<BookResponse> directly or wrapped in ApiResult
     * 
     * @param onlyActive   Filter by active status (optional)
     * @param categoryId   Filter by category ID (optional)
     * @param categoryName Filter by category name (optional)
     * @return List of books (direct array response)
     */
    @GET("api/Book")
    Call<List<BookResponse>> getBooks(
            @Query("onlyActive") Boolean onlyActive,
            @Query("categoryId") String categoryId,
            @Query("categoryName") String categoryName);

    /**
     * Get book by ID
     * 
     * @param bookId Book ID (UUID)
     * @return Book details
     */
    @GET("api/Book/{bookId}")
    Call<BookResponse> getBookById(@Path("bookId") String bookId);

    /**
     * Get all categories
     * 
     * @return List of categories
     */
    @GET("api/Book/Categories")
    Call<List<CategoryResponse>> getCategories();

    /**
     * Get category by ID
     * 
     * @param categoryId Category ID (UUID)
     * @return Category details
     */
    @GET("api/Book/Categories/{categoryId}")
    Call<CategoryResponse> getCategoryById(@Path("categoryId") String categoryId);

    /**
     * Get all chapters for a book
     * 
     * @param bookId Book ID (UUID)
     * @return List of book chapters
     */
    @GET("api/Book/{bookId}/chapters")
    Call<List<BookChapterResponse>> getBookChapters(@Path("bookId") String bookId);
}
