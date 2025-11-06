// app/src/main/java/com/example/LearnMate/network/RetrofitClient.java
package com.example.LearnMate.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.LearnMate.network.api.AiChatService;
import com.example.LearnMate.network.api.AiHighlightService;
import com.example.LearnMate.network.api.AiTranslateService;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.api.PayOSService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    // Sử dụng ApiConfig để lấy BASE_URL tập trung
    // Tất cả URL được quản lý tại ApiConfig.java - chỉ cần thay đổi ở đó khi deploy
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private static final String AI_CHAT_BASE_URL = ApiConfig.AI_CHAT_BASE_URL;
    private static final String AI_TRANSLATE_BASE_URL = ApiConfig.AI_TRANSLATE_BASE_URL;

    // Retrofit “thuần” (không header Authorization)
    private static Retrofit plainRetrofit;

    // Retrofit có kèm interceptor Authorization
    private static Retrofit retrofitWithAuth;

    // Retrofit riêng cho AI chat (không auth)
    private static Retrofit aiChatRetrofit;

    // Retrofit riêng cho AI translate (có auth)
    private static Retrofit aiTranslateRetrofit;

    // Cached services
    private static AuthService cachedAuthService;
    private static AiChatService cachedAiChatService;
    private static AiHighlightService cachedAiHighlightService;
    private static AiTranslateService cachedAiTranslateService;
    private static PayOSService cachedPayOSService;

    private RetrofitClient() {
    }

    // ------------------------
    // Clients
    // ------------------------

    /** OkHttp client có logging, không đính kèm token */
    private static OkHttpClient buildPlainClient() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(60, TimeUnit.SECONDS) // Tăng timeout cho Google Drive
                .readTimeout(300, TimeUnit.SECONDS) // Tăng timeout cho file lớn
                .writeTimeout(300, TimeUnit.SECONDS) // Tăng timeout cho upload
                .retryOnConnectionFailure(true)
                .build();
    }

    /** OkHttp client có logging + tự thêm Authorization: Bearer <token> nếu có */
    private static OkHttpClient getAuthenticatedClient(Context appContext) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();

            SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

            // Ưu tiên key mới "user_token", fallback về "token" (legacy)
            String token = sp.getString("user_token", null);
            if (token == null || token.isEmpty()) {
                token = sp.getString("token", null); // Fallback cho compatibility
            }

            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
                android.util.Log.d("RetrofitClient", "Adding Bearer token to request");
            } else {
                android.util.Log.w("RetrofitClient", "No token found! Request will fail with 401");
            }

            builder.header("Accept", "application/json");

            return chain.proceed(builder.build());
        };

        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(log)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS) // upload có thể lâu
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();
    }

    // ------------------------
    // Retrofit factories
    // ------------------------

    /**
     * Lấy Retrofit “thuần” theo BASE_URL — dùng ở nơi bạn gọi RetrofitClient.get().
     */
    public static Retrofit get() {
        if (plainRetrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();
            plainRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(buildPlainClient())
                    .build();
        }
        return plainRetrofit;
    }

    /** (Tuỳ chọn) Nếu cần Retrofit có auth để tự tạo service riêng. */
    public static Retrofit getRetrofitWithAuth(Context appContext) {
        // Always recreate to ensure latest token is used
        // Don't cache to avoid stale token issues
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getAuthenticatedClient(appContext))
                .build();
        
        // Still cache for performance, but refresh when needed
        if (retrofitWithAuth == null) {
            retrofitWithAuth = retrofit;
        }
        
        return retrofit;
    }

    /**
     * Clear cache của Retrofit instances để tạo fresh connection
     * Nên gọi sau khi upload file lớn hoặc khi gặp lỗi 503
     */
    public static void clearCache() {
        plainRetrofit = null;
        retrofitWithAuth = null;
        aiChatRetrofit = null;
        aiTranslateRetrofit = null;
        
        // Clear cached services
        cachedAuthService = null;
        cachedAiChatService = null;
        cachedAiHighlightService = null;
        cachedAiTranslateService = null;
        cachedPayOSService = null;
        
        android.util.Log.d("RetrofitClient", "Cleared all Retrofit cache");
    }

    // ------------------------
    // Typed services (giữ nguyên APIs bạn đang dùng)
    // ------------------------

    public static AuthService getAuthService(Context appContext) {
        if (cachedAuthService == null) {
            if (retrofitWithAuth == null) {
                Gson gson = new GsonBuilder().setLenient().create();
                retrofitWithAuth = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getAuthenticatedClient(appContext))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            cachedAuthService = retrofitWithAuth.create(AuthService.class);
        }
        return cachedAuthService;
    }

    // Giữ lại nếu app còn dùng AI chat (không auth)
    public static AiChatService getAiChatService(Context appContext) {
        if (cachedAiChatService == null) {
            if (aiChatRetrofit == null) {
                Gson gson = new GsonBuilder().setLenient().create();
                aiChatRetrofit = new Retrofit.Builder()
                        .baseUrl(AI_CHAT_BASE_URL)
                        .client(buildPlainClient())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            cachedAiChatService = aiChatRetrofit.create(AiChatService.class);
        }
        return cachedAiChatService;
    }

    // Dịch vụ upload/translate — dùng client có auth
    public static AiTranslateService getAiTranslateService(Context appContext) {
        if (cachedAiTranslateService == null) {
            if (aiTranslateRetrofit == null) {
                Gson gson = new GsonBuilder().setLenient().create();
                aiTranslateRetrofit = new Retrofit.Builder()
                        .baseUrl(AI_TRANSLATE_BASE_URL)
                        .client(getAuthenticatedClient(appContext))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            cachedAiTranslateService = aiTranslateRetrofit.create(AiTranslateService.class);
        }
        return cachedAiTranslateService;
    }

    // Dịch vụ AI Highlight — dùng client không auth (giống AI Chat)
    public static AiHighlightService getAiHighlightService(Context appContext) {
        if (cachedAiHighlightService == null) {
            if (aiChatRetrofit == null) {
                Gson gson = new GsonBuilder().setLenient().create();
                aiChatRetrofit = new Retrofit.Builder()
                        .baseUrl(AI_CHAT_BASE_URL)
                        .client(buildPlainClient())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            cachedAiHighlightService = aiChatRetrofit.create(AiHighlightService.class);
        }
        return cachedAiHighlightService;
    }
    
    // Dịch vụ PayOS — dùng client có auth (vì cần user context)
    public static PayOSService getPayOSService(Context appContext) {
        if (cachedPayOSService == null) {
            if (retrofitWithAuth == null) {
                Gson gson = new GsonBuilder().setLenient().create();
                retrofitWithAuth = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getAuthenticatedClient(appContext))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            cachedPayOSService = retrofitWithAuth.create(PayOSService.class);
        }
        return cachedPayOSService;
    }
}
