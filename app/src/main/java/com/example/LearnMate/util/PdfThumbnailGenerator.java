package com.example.LearnMate.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.InputStream;

/**
 * Utility class to generate thumbnails from PDF files
 */
public class PdfThumbnailGenerator {
    private static final String TAG = "PdfThumbnailGen";
    private static final int THUMBNAIL_WIDTH = 400; // Width của thumbnail trong pixels
    private static final float DPI = 72; // DPI cho rendering

    /**
     * Generate thumbnail từ trang đầu tiên của PDF
     * 
     * @param context Context để access content resolver
     * @param pdfUri  URI của PDF file
     * @return Bitmap của trang đầu tiên, hoặc null nếu có lỗi
     */
    public static Bitmap generateThumbnail(Context context, Uri pdfUri) {
        try {
            // Initialize PDFBox
            PDFBoxResourceLoader.init(context);

            Log.d(TAG, "Generating thumbnail for URI: " + pdfUri.toString());

            // Open input stream from URI
            try (InputStream inputStream = context.getContentResolver().openInputStream(pdfUri)) {
                if (inputStream == null) {
                    Log.e(TAG, "Cannot open input stream for URI: " + pdfUri);
                    return null;
                }

                // Load PDF document
                PDDocument document = PDDocument.load(inputStream);

                if (document.getNumberOfPages() == 0) {
                    Log.e(TAG, "PDF has no pages");
                    document.close();
                    return null;
                }

                // Render first page
                PDFRenderer renderer = new PDFRenderer(document);

                // Tính toán scale factor dựa trên width mong muốn
                float scale = THUMBNAIL_WIDTH / renderer.renderImage(0, 1).getWidth();

                // Render page với scale factor
                Bitmap bitmap = renderer.renderImage(0, scale);

                Log.d(TAG, "Thumbnail generated successfully: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                document.close();
                return bitmap;

            } catch (Exception e) {
                Log.e(TAG, "Error reading PDF: " + e.getMessage(), e);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate thumbnail bất đồng bộ với callback
     */
    public interface ThumbnailCallback {
        void onThumbnailGenerated(Bitmap bitmap);

        void onError(Exception e);
    }

    public static void generateThumbnailAsync(Context context, Uri pdfUri, ThumbnailCallback callback) {
        new Thread(() -> {
            try {
                Bitmap thumbnail = generateThumbnail(context, pdfUri);
                if (thumbnail != null) {
                    callback.onThumbnailGenerated(thumbnail);
                } else {
                    callback.onError(new Exception("Failed to generate thumbnail"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}

