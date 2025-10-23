package com.example.LearnMate.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.LearnMate.network.api.AuthService;

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
    private static final String BASE_URL = "http://chooy-alb-585589353.us-east-1.elb.amazonaws.com";

    private static Retrofit retrofit;
    private static AuthService cachedService;

    private RetrofitClient() {}

    public static AuthService getAuthService(Context appContext) {
        if (cachedService == null) {
            retrofit = buildRetrofit(appContext.getApplicationContext());
            cachedService = retrofit.create(AuthService.class);
        }
        return cachedService;
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
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL) // nhớ dấu '/'
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}

