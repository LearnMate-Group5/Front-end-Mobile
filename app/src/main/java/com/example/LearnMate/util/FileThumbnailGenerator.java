package com.example.LearnMate.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

/**
 * Utility class to generate thumbnails from various file types:
 * - PDF files: Uses PdfThumbnailGenerator
 * - PNG/JPG files: Decodes and resizes the image
 * - DOC files: Returns a default document icon
 */
public class FileThumbnailGenerator {
    private static final String TAG = "FileThumbnailGen";
    private static final int THUMBNAIL_WIDTH = 400; // Width của thumbnail trong pixels
    private static final int THUMBNAIL_HEIGHT = 400; // Height của thumbnail trong pixels

    /**
     * Generate thumbnail từ file dựa trên MIME type
     * 
     * @param context Context để access content resolver
     * @param fileUri URI của file
     * @param mimeType MIME type của file
     * @return Bitmap thumbnail, hoặc null nếu có lỗi
     */
    public static Bitmap generateThumbnail(Context context, Uri fileUri, String mimeType) {
        if (mimeType == null) {
            mimeType = FileUtils.getMimeType(context, fileUri);
        }

        Log.d(TAG, "Generating thumbnail for URI: " + fileUri.toString() + ", MIME: " + mimeType);

        // Xử lý PDF files
        if (mimeType != null && mimeType.equals("application/pdf")) {
            return PdfThumbnailGenerator.generateThumbnail(context, fileUri);
        }

        // Xử lý image files (PNG, JPG, JPEG)
        if (mimeType != null && (mimeType.startsWith("image/"))) {
            return generateImageThumbnail(context, fileUri);
        }

        // Xử lý DOC files - trả về null để dùng default icon
        if (mimeType != null && (mimeType.equals("application/msword") || 
                                  mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            Log.d(TAG, "DOC file detected, returning null for default icon");
            return null; // Sẽ dùng default icon trong adapter
        }

        // Default: thử decode như image
        Log.d(TAG, "Unknown file type, trying to decode as image");
        return generateImageThumbnail(context, fileUri);
    }

    /**
     * Generate thumbnail từ image file (PNG, JPG, JPEG)
     */
    private static Bitmap generateImageThumbnail(Context context, Uri imageUri) {
        InputStream inputStream1 = null;
        InputStream inputStream2 = null;
        try {
            // Mở input stream để đọc bounds
            inputStream1 = context.getContentResolver().openInputStream(imageUri);
            if (inputStream1 == null) {
                Log.e(TAG, "Cannot open input stream for URI: " + imageUri);
                return null;
            }

            // Decode image với options để giảm memory usage
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream1, null, options);
            inputStream1.close();
            inputStream1 = null;

            // Tính toán sample size để resize image
            int scale = calculateInSampleSize(options, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

            // Decode lại với sample size - cần stream mới
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Giảm memory usage

            inputStream2 = context.getContentResolver().openInputStream(imageUri);
            if (inputStream2 == null) {
                Log.e(TAG, "Cannot open input stream for URI (second attempt): " + imageUri);
                return null;
            }
            
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream2, null, options);
            inputStream2.close();
            inputStream2 = null;

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image");
                return null;
            }

            // Resize nếu cần
            if (bitmap.getWidth() > THUMBNAIL_WIDTH || bitmap.getHeight() > THUMBNAIL_HEIGHT) {
                bitmap = Bitmap.createScaledBitmap(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true);
            }

            Log.d(TAG, "Image thumbnail generated successfully: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error generating image thumbnail: " + e.getMessage(), e);
            return null;
        } finally {
            // Đảm bảo đóng streams
            try {
                if (inputStream1 != null) {
                    inputStream1.close();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
            }
        }
    }

    /**
     * Tính toán sample size để resize image
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Generate thumbnail bất đồng bộ với callback
     */
    public interface ThumbnailCallback {
        void onThumbnailGenerated(Bitmap bitmap);
        void onError(Exception e);
    }

    public static void generateThumbnailAsync(Context context, Uri fileUri, String mimeType, ThumbnailCallback callback) {
        new Thread(() -> {
            try {
                Bitmap thumbnail = generateThumbnail(context, fileUri, mimeType);
                if (thumbnail != null) {
                    callback.onThumbnailGenerated(thumbnail);
                } else {
                    // Đối với DOC files, trả về null là bình thường
                    callback.onThumbnailGenerated(null);
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
