package com.example.LearnMate.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.LearnMate.network.dto.BookResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager để lưu và quản lý sách yêu thích của user
 */
public class FavoriteBookManager {
    private static final String PREF_NAME = "favorite_books";
    private static final String KEY_FAVORITE_BOOKS = "favorite_books_list";

    private final SharedPreferences prefs;
    private final Gson gson;

    public FavoriteBookManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Thêm sách vào danh sách yêu thích
     */
    public void addFavorite(BookResponse book) {
        if (book == null || book.bookId == null) {
            return;
        }

        List<BookResponse> favorites = getFavorites();
        
        // Kiểm tra xem sách đã tồn tại chưa
        for (BookResponse b : favorites) {
            if (b.bookId != null && b.bookId.equals(book.bookId)) {
                // Đã tồn tại, không thêm nữa
                return;
            }
        }

        // Thêm sách mới
        favorites.add(book);
        saveFavorites(favorites);
        android.util.Log.d("FavoriteBookManager", "Added favorite: " + book.title);
    }

    /**
     * Xóa sách khỏi danh sách yêu thích
     */
    public void removeFavorite(String bookId) {
        if (bookId == null || bookId.isEmpty()) {
            return;
        }

        List<BookResponse> favorites = getFavorites();
        favorites.removeIf(b -> b.bookId != null && b.bookId.equals(bookId));
        
        saveFavorites(favorites);
        android.util.Log.d("FavoriteBookManager", "Removed favorite: " + bookId);
    }

    /**
     * Kiểm tra xem sách có được yêu thích không
     */
    public boolean isFavorite(String bookId) {
        if (bookId == null || bookId.isEmpty()) {
            return false;
        }

        List<BookResponse> favorites = getFavorites();
        for (BookResponse b : favorites) {
            if (b.bookId != null && b.bookId.equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Toggle favorite (thêm nếu chưa có, xóa nếu đã có)
     */
    public boolean toggleFavorite(BookResponse book) {
        if (book == null || book.bookId == null) {
            return false;
        }

        if (isFavorite(book.bookId)) {
            removeFavorite(book.bookId);
            return false; // Đã xóa
        } else {
            addFavorite(book);
            return true; // Đã thêm
        }
    }

    /**
     * Lấy tất cả sách yêu thích
     */
    public List<BookResponse> getFavorites() {
        String json = prefs.getString(KEY_FAVORITE_BOOKS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<BookResponse>>() {}.getType();
            List<BookResponse> favorites = gson.fromJson(json, type);
            return favorites != null ? favorites : new ArrayList<>();
        } catch (Exception e) {
            android.util.Log.e("FavoriteBookManager", "Error loading favorites: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Xóa tất cả sách yêu thích
     */
    public void clearAllFavorites() {
        prefs.edit().remove(KEY_FAVORITE_BOOKS).apply();
        android.util.Log.d("FavoriteBookManager", "All favorites cleared");
    }

    /**
     * Lưu danh sách yêu thích vào SharedPreferences
     */
    private void saveFavorites(List<BookResponse> favorites) {
        String json = gson.toJson(favorites);
        prefs.edit().putString(KEY_FAVORITE_BOOKS, json).apply();
    }
}

