package com.example.LearnMate.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.api.AiChatService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    // Theo "servers" trong OpenAPI
    // Use 10.0.2.2 to connect to host machine's localhost from Android Emulator
    private static final String BASE_URL = "http://10.0.2.2:2406/";
    private static final String AI_CHAT_BASE_URL = "http://10.0.2.2:5678/";

    private static Retrofit retrofit;
    private static Retrofit aiChatRetrofit;
    private static AuthService cachedService;
    private static AiChatService cachedAiChatService;

    private RetrofitClient() {}

    public static AuthService getAuthService(Context appContext) {
        if (cachedService == null) {
            retrofit = buildRetrofit(appContext.getApplicationContext());
            cachedService = retrofit.create(AuthService.class);
        }
        return cachedService;
    }

    public static AiChatService getAiChatService(Context appContext) {
        if (cachedAiChatService == null) {
            aiChatRetrofit = buildAiChatRetrofit(appContext.getApplicationContext());
            cachedAiChatService = aiChatRetrofit.create(AiChatService.class);
        }
        return cachedAiChatService;
    }

    private static Retrofit buildRetrofit(Context appContext) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Interceptor gắn Authorization nếu có token trong SharedPreferences
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();

            SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String token = sp.getString("token", null);
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
            // Đảm bảo accept json
            builder.header("Accept", "application/json");

            // Optional: giữ nguyên query/path
            HttpUrl url = original.url();
            builder.url(url);

            return chain.proceed(builder.build());
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(log)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS) // 1 minute for auth
                .writeTimeout(60, TimeUnit.SECONDS) // 1 minute for auth
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL) // nhớ dấu '/'
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static Retrofit buildAiChatRetrofit(Context appContext) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS) // 5 minutes
                .writeTimeout(300, TimeUnit.SECONDS) // 5 minutes
                .build();

        return new Retrofit.Builder()
                .baseUrl(AI_CHAT_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
