package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.BookChapterResponse;
import com.example.LearnMate.network.dto.BookResponse;
import com.example.LearnMate.network.dto.CategoryResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;

public interface BookService {
    /**
     * Lấy danh sách tất cả books
     * GET /api/Book
     * 
     * @param onlyActive - Chỉ lấy sách đang active (optional)
     * @param categoryId - Lọc theo category ID (optional)
     * @param categoryName - Lọc theo category name (optional)
     * @return Danh sách books
     */
    @GET("api/Book")
    Call<List<BookResponse>> getBooks(
        @Query("onlyActive") Boolean onlyActive,
        @Query("categoryId") String categoryId,
        @Query("categoryName") String categoryName
    );

    /**
     * Lấy danh sách tất cả books (không có filter)
     * GET /api/Book
     * 
     * @return Danh sách books
     */
    @GET("api/Book")
    Call<List<BookResponse>> getBooks();

    /**
     * Lấy thông tin một book theo ID
     * GET /api/Book/{bookId}
     * 
     * @param bookId - ID của book
     * @return Thông tin book
     */
    @GET("api/Book/{bookId}")
    Call<BookResponse> getBookById(@Path("bookId") String bookId);

    /**
     * Lấy danh sách chapters của một book
     * GET /api/Book/{bookId}/chapters
     * 
     * @param bookId - ID của book
     * @return Danh sách chapters
     */
    @GET("api/Book/{bookId}/chapters")
    Call<List<BookChapterResponse>> getBookChapters(@Path("bookId") String bookId);

    /**
     * Lấy danh sách tất cả categories
     * GET /api/Book/Categories
     * 
     * @return Danh sách categories
     */
    @GET("api/Book/Categories")
    Call<List<CategoryResponse>> getCategories();
}
