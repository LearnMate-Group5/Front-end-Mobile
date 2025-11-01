package com.example.LearnMate.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager để lưu và quản lý bookmarks cho chapters
 * Bookmark được lưu per file PDF
 */
public class BookmarkManager {
    private static final String PREF_NAME = "bookmarks";
    private static final String KEY_BOOKMARKS = "bookmark_list";

    private final SharedPreferences prefs;
    private final Gson gson;

    public BookmarkManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Model cho một bookmark
     */
    public static class Bookmark {
        public String fileUri; // URI của file PDF
        public String fileName; // Tên file
        public int chapterIndex; // Index của chapter
        public String chapterTitle; // Title của chapter
        public long bookmarkedAt; // Timestamp khi bookmark

        public Bookmark() {
        }

        public Bookmark(String fileUri, String fileName, int chapterIndex, String chapterTitle) {
            this.fileUri = fileUri;
            this.fileName = fileName;
            this.chapterIndex = chapterIndex;
            this.chapterTitle = chapterTitle;
            this.bookmarkedAt = System.currentTimeMillis();
        }
    }

    /**
     * Thêm bookmark cho một chapter
     */
    public void addBookmark(String fileUri, String fileName, int chapterIndex, String chapterTitle) {
        List<Bookmark> bookmarks = getBookmarks();

        // Kiểm tra xem bookmark đã tồn tại chưa
        for (Bookmark b : bookmarks) {
            if (b.fileUri.equals(fileUri) && b.chapterIndex == chapterIndex) {
                // Đã tồn tại, không thêm nữa
                return;
            }
        }

        // Thêm bookmark mới
        Bookmark newBookmark = new Bookmark(fileUri, fileName, chapterIndex, chapterTitle);
        bookmarks.add(newBookmark);

        saveBookmarks(bookmarks);
        android.util.Log.d("BookmarkManager", "Added bookmark: " + chapterTitle + " (Chapter " + chapterIndex + ")");
    }

    /**
     * Xóa bookmark cho một chapter
     */
    public void removeBookmark(String fileUri, int chapterIndex) {
        List<Bookmark> bookmarks = getBookmarks();

        bookmarks.removeIf(b -> b.fileUri.equals(fileUri) && b.chapterIndex == chapterIndex);

        saveBookmarks(bookmarks);
        android.util.Log.d("BookmarkManager", "Removed bookmark for chapter " + chapterIndex);
    }

    /**
     * Kiểm tra xem chapter có được bookmark không
     */
    public boolean isBookmarked(String fileUri, int chapterIndex) {
        List<Bookmark> bookmarks = getBookmarks();

        for (Bookmark b : bookmarks) {
            if (b.fileUri.equals(fileUri) && b.chapterIndex == chapterIndex) {
                return true;
            }
        }

        return false;
    }

    /**
     * Toggle bookmark (thêm nếu chưa có, xóa nếu đã có)
     */
    public boolean toggleBookmark(String fileUri, String fileName, int chapterIndex, String chapterTitle) {
        if (isBookmarked(fileUri, chapterIndex)) {
            removeBookmark(fileUri, chapterIndex);
            return false; // Đã xóa
        } else {
            addBookmark(fileUri, fileName, chapterIndex, chapterTitle);
            return true; // Đã thêm
        }
    }

    /**
     * Lấy tất cả bookmarks
     */
    public List<Bookmark> getBookmarks() {
        String json = prefs.getString(KEY_BOOKMARKS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<Bookmark>>() {
            }.getType();
            List<Bookmark> bookmarks = gson.fromJson(json, type);
            return bookmarks != null ? bookmarks : new ArrayList<>();
        } catch (Exception e) {
            android.util.Log.e("BookmarkManager", "Error loading bookmarks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy bookmarks cho một file cụ thể
     */
    public List<Bookmark> getBookmarksForFile(String fileUri) {
        List<Bookmark> allBookmarks = getBookmarks();
        List<Bookmark> fileBookmarks = new ArrayList<>();

        for (Bookmark b : allBookmarks) {
            if (b.fileUri.equals(fileUri)) {
                fileBookmarks.add(b);
            }
        }

        return fileBookmarks;
    }

    /**
     * Lấy số lượng bookmarks cho một file
     */
    public int getBookmarkCountForFile(String fileUri) {
        return getBookmarksForFile(fileUri).size();
    }

    /**
     * Xóa tất cả bookmarks cho một file
     */
    public void removeAllBookmarksForFile(String fileUri) {
        List<Bookmark> bookmarks = getBookmarks();
        bookmarks.removeIf(b -> b.fileUri.equals(fileUri));
        saveBookmarks(bookmarks);
        android.util.Log.d("BookmarkManager", "Removed all bookmarks for file: " + fileUri);
    }

    /**
     * Xóa tất cả bookmarks
     */
    public void clearAllBookmarks() {
        prefs.edit().clear().apply();
        android.util.Log.d("BookmarkManager", "All bookmarks cleared");
    }

    /**
     * Lưu bookmarks vào SharedPreferences
     */
    private void saveBookmarks(List<Bookmark> bookmarks) {
        String json = gson.toJson(bookmarks);
        prefs.edit().putString(KEY_BOOKMARKS, json).apply();
    }

    /**
     * Lấy bookmark gần nhất của một file
     */
    public Bookmark getLatestBookmarkForFile(String fileUri) {
        List<Bookmark> fileBookmarks = getBookmarksForFile(fileUri);

        if (fileBookmarks.isEmpty()) {
            return null;
        }

        // Sắp xếp theo thời gian, lấy cái mới nhất
        fileBookmarks.sort((b1, b2) -> Long.compare(b2.bookmarkedAt, b1.bookmarkedAt));
        return fileBookmarks.get(0);
    }
}

