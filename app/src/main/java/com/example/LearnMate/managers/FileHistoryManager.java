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
 * Manager để lưu và load lịch sử files đã import theo userId
 */
public class FileHistoryManager {
    private static final String PREF_NAME = "file_history";
    private static final String KEY_IMPORTED_FILES_PREFIX = "imported_files_";

    private final SharedPreferences prefs;
    private final Gson gson;
    private final String userId;

    public FileHistoryManager(Context context) {
        this(context, null);
    }

    public FileHistoryManager(Context context, String userId) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.userId = userId != null ? userId : getCurrentUserId(context);
    }

    /**
     * Lấy userId hiện tại từ SharedPreferences
     */
    private String getCurrentUserId(Context context) {
        SharedPreferences userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userId = userPrefs.getString("user_id", null);
        if (userId == null) {
            // Try to get from user_data JSON
            String userDataJson = userPrefs.getString("user_data", null);
            if (userDataJson != null) {
                try {
                    com.example.LearnMate.network.dto.LoginResponse.UserData userData = 
                        gson.fromJson(userDataJson, com.example.LearnMate.network.dto.LoginResponse.UserData.class);
                    if (userData != null && userData.getUserId() != null) {
                        return userData.getUserId();
                    }
                } catch (Exception e) {
                    android.util.Log.e("FileHistoryManager", "Error parsing user_data: " + e.getMessage());
                }
            }
        }
        return userId != null ? userId : "default_user";
    }

    /**
     * Lấy key cho userId hiện tại
     */
    private String getKeyForUser() {
        return KEY_IMPORTED_FILES_PREFIX + userId;
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
     * Lấy danh sách files đã import cho userId hiện tại
     */
    public List<ImportedFile> getFiles() {
        String key = getKeyForUser();
        String json = prefs.getString(key, null);
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
     * Lưu toàn bộ danh sách cho userId hiện tại
     */
    private void saveFiles(List<ImportedFile> files) {
        String key = getKeyForUser();
        String json = gson.toJson(files);
        prefs.edit().putString(key, json).apply();
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
     * Xóa toàn bộ lịch sử cho userId hiện tại
     */
    public void clearAll() {
        String key = getKeyForUser();
        prefs.edit().remove(key).apply();
    }

    /**
     * Xóa toàn bộ lịch sử cho tất cả users (dùng khi cần cleanup)
     */
    public void clearAllUsers() {
        SharedPreferences.Editor editor = prefs.edit();
        // Get all keys and remove those starting with prefix
        java.util.Map<String, ?> allPrefs = prefs.getAll();
        for (String key : allPrefs.keySet()) {
            if (key.startsWith(KEY_IMPORTED_FILES_PREFIX)) {
                editor.remove(key);
            }
        }
        editor.apply();
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
