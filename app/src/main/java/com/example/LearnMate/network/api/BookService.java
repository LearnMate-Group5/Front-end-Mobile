package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.BookChapterResponse;
import com.example.LearnMate.network.dto.BookResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

public interface BookService {
    /**
     * Lấy danh sách tất cả books
     * GET /api/Book
     * 
     * @return Danh sách books
     */
    @GET("api/Book")
    Call<List<BookResponse>> getBooks();

    /**
     * Lấy danh sách chapters của một book
     * GET /api/Book/{bookId}/chapters
     * 
     * @param bookId - ID của book
     * @return Danh sách chapters
     */
    @GET("api/Book/{bookId}/chapters")
    Call<List<BookChapterResponse>> getBookChapters(@Path("bookId") String bookId);
}

