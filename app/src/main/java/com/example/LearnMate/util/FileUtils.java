// com/example/LearnMate/util/FileUtils.java
package com.example.LearnMate.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class FileUtils {
    private FileUtils() {
    }

    public static String getDisplayName(Context ctx, Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = ctx.getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0)
                        name = c.getString(idx);
                }
            }
        }
        if (name == null)
            name = new File(uri.getPath()).getName();
        return name;
    }

    public static String getMimeType(Context ctx, Uri uri) {
        String type = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            type = ctx.getContentResolver().getType(uri);
        } else {
            String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (ext != null)
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        }
        return type != null ? type : "application/pdf";
    }

    public static MultipartBody.Part uriToPdfPart(Context ctx, Uri uri, String formName) throws Exception {
        String fileName = getDisplayName(ctx, uri);
        String mime = getMimeType(ctx, uri);

        // Log để debug
        android.util.Log.d("FileUtils", "Processing URI: " + uri.toString());
        android.util.Log.d("FileUtils", "FileName: " + fileName);
        android.util.Log.d("FileUtils", "MimeType: " + mime);

        // Tạo file cache với tên unique để tránh conflict
        String cacheFileName = "upload_" + System.currentTimeMillis() + "_" + fileName;
        File cache = new File(ctx.getCacheDir(), cacheFileName);

        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new Exception("Cannot open input stream for URI: " + uri.toString());
            }

            try (FileOutputStream out = new FileOutputStream(cache)) {
                byte[] buf = new byte[8192];
                int len;
                int totalBytes = 0;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    totalBytes += len;
                }
                android.util.Log.d("FileUtils", "File copied successfully: " + totalBytes + " bytes");
            }
        } catch (Exception e) {
            android.util.Log.e("FileUtils", "Error copying file: " + e.getMessage());
            // Xóa file cache nếu có lỗi
            if (cache.exists()) {
                cache.delete();
            }
            throw e;
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse(mime), cache);
        return MultipartBody.Part.createFormData(formName, fileName, fileBody);
    }

    public static RequestBody textPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}
