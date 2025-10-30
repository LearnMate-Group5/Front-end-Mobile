package com.example.LearnMate.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager để lưu và load lịch sử files đã import
 */
public class FileHistoryManager {
    private static final String PREF_NAME = "file_history";
    private static final String KEY_IMPORTED_FILES = "imported_files";

    private final SharedPreferences prefs;
    private final Gson gson;

    public FileHistoryManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Model để lưu thông tin file đã import
     */
    public static class ImportedFile {
        public String uri; // URI của file
        public String fileName; // Tên file
        public String fileId; // ID từ server (nếu có)
        public String category; // Category phân loại
        public String language; // Ngôn ngữ detect được
        public int totalPages; // Số trang
        public long importedAt; // Timestamp khi import
        public String thumbnailPath; // Path của thumbnail đã lưu

        public ImportedFile() {
        }

        public ImportedFile(String uri, String fileName, String category, String language, int totalPages) {
            this.uri = uri;
            this.fileName = fileName;
            this.category = category;
            this.language = language;
            this.totalPages = totalPages;
            this.importedAt = System.currentTimeMillis();
        }

        public ImportedFile(String uri, String fileName, String category, String language, int totalPages,
                String thumbnailPath) {
            this.uri = uri;
            this.fileName = fileName;
            this.category = category;
            this.language = language;
            this.totalPages = totalPages;
            this.importedAt = System.currentTimeMillis();
            this.thumbnailPath = thumbnailPath;
        }
    }

    /**
     * Lưu file vào lịch sử
     */
    public void addFile(ImportedFile file) {
        List<ImportedFile> files = getFiles();

        // Check duplicate bằng URI
        boolean exists = false;
        for (ImportedFile existing : files) {
            if (existing.uri != null && existing.uri.equals(file.uri)) {
                exists = true;
                break;
            }
        }

        // Chỉ thêm nếu chưa tồn tại
        if (!exists) {
            files.add(0, file); // Thêm vào đầu list (mới nhất)
        }

        saveFiles(files);
    }

    /**
     * Lấy danh sách files đã import
     */
    public List<ImportedFile> getFiles() {
        String json = prefs.getString(KEY_IMPORTED_FILES, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<ImportedFile>>() {
            }.getType();
            List<ImportedFile> files = gson.fromJson(json, type);
            return files != null ? files : new ArrayList<>();
        } catch (Exception e) {
            android.util.Log.e("FileHistoryManager", "Error parsing files: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lưu toàn bộ danh sách
     */
    private void saveFiles(List<ImportedFile> files) {
        String json = gson.toJson(files);
        prefs.edit().putString(KEY_IMPORTED_FILES, json).apply();
    }

    /**
     * Xóa file khỏi lịch sử
     */
    public void removeFile(String uri) {
        List<ImportedFile> files = getFiles();
        files.removeIf(file -> file.uri != null && file.uri.equals(uri));
        saveFiles(files);
    }

    /**
     * Xóa toàn bộ lịch sử
     */
    public void clearAll() {
        prefs.edit().remove(KEY_IMPORTED_FILES).apply();
    }

    /**
     * Kiểm tra file đã import chưa
     */
    public boolean isImported(String uri) {
        List<ImportedFile> files = getFiles();
        for (ImportedFile file : files) {
            if (file.uri != null && file.uri.equals(uri)) {
                return true;
            }
        }
        return false;
    }
}
