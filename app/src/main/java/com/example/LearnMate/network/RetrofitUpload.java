package com.example.LearnMate.network;

import android.content.Context;
import android.net.Uri;

import com.example.LearnMate.network.api.UploadApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUpload {
    private static UploadApi instance;

    // Sử dụng ApiConfig để lấy BASE_URL tập trung
    // Tất cả service (bao gồm N8N webhooks) đều dùng chung BASE_URL
    // URL được quản lý tại ApiConfig.java - chỉ cần thay đổi ở đó khi deploy
    private static final String BASE_URL = ApiConfig.BASE_URL;

    public static UploadApi api() {
        if (instance == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(log).build();

            Retrofit r = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            instance = r.create(UploadApi.class);
        }
        return instance;
    }

    public static File tempFileFromUri(Context ctx, Uri uri) throws Exception {
        InputStream in = ctx.getContentResolver().openInputStream(uri);
        File out = File.createTempFile("upload_", ".pdf", ctx.getCacheDir());
        FileOutputStream fo = new FileOutputStream(out);
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) fo.write(buf, 0, n);
        in.close(); fo.flush(); fo.close();
        return out;
    }

    public static MultipartBody.Part asPart(File f) {
        RequestBody body = RequestBody.create(MediaType.parse("application/pdf"), f);
        return MultipartBody.Part.createFormData("File", f.getName(), body); // field "File" theo n8n của bạn
    }

    public static RequestBody asText(String s) {
        return RequestBody.create(MediaType.parse("text/plain"), s == null ? "" : s);
    }
}
