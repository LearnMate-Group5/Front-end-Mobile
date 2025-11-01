package com.example.LearnMate.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility class để lưu và load PDF thumbnails từ disk
 */
public class ThumbnailCache {
    private static final String TAG = "ThumbnailCache";
    private static final String CACHE_DIR = "pdf_thumbnails";
    private static final int QUALITY = 85; // JPEG quality (0-100)

    /**
     * Lưu thumbnail vào internal storage
     * 
     * @param context Context
     * @param bitmap  Thumbnail bitmap
     * @param fileId  Unique ID cho file (dùng URI hash hoặc filename)
     * @return Path của file đã lưu, hoặc null nếu lỗi
     */
    public static String saveThumbnail(Context context, Bitmap bitmap, String fileId) {
        if (bitmap == null || fileId == null) {
            Log.e(TAG, "Bitmap or fileId is null");
            return null;
        }

        try {
            // Tạo cache directory
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            // Tạo file name từ fileId (clean special chars)
            String fileName = sanitizeFileName(fileId) + ".jpg";
            File thumbnailFile = new File(cacheDir, fileName);

            // Save bitmap to file
            try (FileOutputStream fos = new FileOutputStream(thumbnailFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
                fos.flush();
            }

            Log.d(TAG, "Thumbnail saved: " + thumbnailFile.getAbsolutePath());
            return thumbnailFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error saving thumbnail: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load thumbnail từ disk
     * 
     * @param context Context
     * @param fileId  Unique ID của file
     * @return Bitmap hoặc null nếu không tìm thấy
     */
    public static Bitmap loadThumbnail(Context context, String fileId) {
        if (fileId == null) {
            return null;
        }

        try {
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR);
            String fileName = sanitizeFileName(fileId) + ".jpg";
            File thumbnailFile = new File(cacheDir, fileName);

            if (!thumbnailFile.exists()) {
                Log.w(TAG, "Thumbnail not found: " + fileName);
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(thumbnailFile.getAbsolutePath());
            Log.d(TAG, "Thumbnail loaded: " + fileName);
            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error loading thumbnail: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Load thumbnail từ absolute path
     */
    public static Bitmap loadThumbnailFromPath(String absolutePath) {
        if (absolutePath == null) {
            return null;
        }

        try {
            File file = new File(absolutePath);
            if (!file.exists()) {
                Log.w(TAG, "Thumbnail file not found: " + absolutePath);
                return null;
            }

            return BitmapFactory.decodeFile(absolutePath);
        } catch (Exception e) {
            Log.e(TAG, "Error loading thumbnail from path: " + e.getMessage());
            return null;
        }
    }

    /**
     * Xóa thumbnail
     */
    public static boolean deleteThumbnail(Context context, String fileId) {
        if (fileId == null) {
            return false;
        }

        try {
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR);
            String fileName = sanitizeFileName(fileId) + ".jpg";
            File thumbnailFile = new File(cacheDir, fileName);

            if (thumbnailFile.exists()) {
                boolean deleted = thumbnailFile.delete();
                Log.d(TAG, "Thumbnail deleted: " + fileName + " = " + deleted);
                return deleted;
            }
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error deleting thumbnail: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa tất cả thumbnails
     */
    public static void clearAllThumbnails(Context context) {
        try {
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR);
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                Log.d(TAG, "All thumbnails cleared");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing thumbnails: " + e.getMessage());
        }
    }

    /**
     * Lấy kích thước cache
     */
    public static long getCacheSize(Context context) {
        long size = 0;
        try {
            File cacheDir = new File(context.getFilesDir(), CACHE_DIR);
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        size += file.length();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache size: " + e.getMessage());
        }
        return size;
    }

    /**
     * Clean file name (remove special characters)
     */
    private static String sanitizeFileName(String name) {
        // Remove special characters, keep only alphanumeric and underscore
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Generate unique ID từ URI
     */
    public static String generateFileId(String uri) {
        if (uri == null) {
            return "unknown_" + System.currentTimeMillis();
        }
        // Use hash code as unique ID
        return "pdf_" + Math.abs(uri.hashCode());
    }
}

