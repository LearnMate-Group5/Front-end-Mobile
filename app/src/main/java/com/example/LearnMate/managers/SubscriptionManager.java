package com.example.LearnMate.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager để quản lý thông tin subscription hiện tại
 * Tự động load subscription khi có token và cache vào SharedPreferences
 */
public class SubscriptionManager {
    private static final String TAG = "SubscriptionManager";
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_CURRENT_SUBSCRIPTION = "current_subscription";
    
    private final Context appContext;
    private final SharedPreferences pref;
    private final SubscriptionService subscriptionService;
    private final Gson gson;
    
    private static SubscriptionManager instance;
    private CurrentSubscriptionResponse cachedSubscription;
    
    private SubscriptionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.subscriptionService = RetrofitClient.getSubscriptionService(appContext);
        this.gson = new Gson();
        loadCachedSubscription();
    }
    
    public static synchronized SubscriptionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SubscriptionManager(context);
        }
        return instance;
    }
    
    /**
     * Load subscription từ cache (SharedPreferences)
     */
    private void loadCachedSubscription() {
        String subscriptionJson = pref.getString(KEY_CURRENT_SUBSCRIPTION, null);
        if (subscriptionJson != null) {
            try {
                cachedSubscription = gson.fromJson(subscriptionJson, CurrentSubscriptionResponse.class);
                Log.d(TAG, "Loaded subscription from cache: " + (cachedSubscription != null ? cachedSubscription.name : "null"));
            } catch (Exception e) {
                Log.e(TAG, "Error loading subscription from cache", e);
                cachedSubscription = null;
            }
        }
    }
    
    /**
     * Lưu subscription vào cache
     */
    private void saveSubscriptionToCache(CurrentSubscriptionResponse subscription) {
        if (subscription != null) {
            String subscriptionJson = gson.toJson(subscription);
            pref.edit().putString(KEY_CURRENT_SUBSCRIPTION, subscriptionJson).apply();
            cachedSubscription = subscription;
            Log.d(TAG, "Saved subscription to cache: " + subscription.name);
        } else {
            pref.edit().remove(KEY_CURRENT_SUBSCRIPTION).apply();
            cachedSubscription = null;
            Log.d(TAG, "Removed subscription from cache");
        }
    }
    
    /**
     * Load subscription từ API và cache lại
     * Gọi ngay sau khi login thành công
     */
    public void loadSubscriptionFromAPI() {
        SessionManager sessionManager = new SessionManager(appContext);
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping subscription load");
            return;
        }
        
        Log.d(TAG, "Loading subscription from API...");
        subscriptionService.getCurrentSubscription().enqueue(new Callback<CurrentSubscriptionResponse>() {
            @Override
            public void onResponse(Call<CurrentSubscriptionResponse> call, Response<CurrentSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentSubscriptionResponse subscription = response.body();
                    saveSubscriptionToCache(subscription);
                    Log.d(TAG, "Subscription loaded successfully: " + subscription.name);
                } else {
                    // Không có subscription hoặc đã hết hạn
                    saveSubscriptionToCache(null);
                    Log.d(TAG, "No active subscription found - Response code: " + (response != null ? response.code() : "null"));
                }
            }

            @Override
            public void onFailure(Call<CurrentSubscriptionResponse> call, Throwable t) {
                Log.e(TAG, "Error loading subscription from API: " + t.getMessage(), t);
                // Giữ nguyên cache nếu có, không xóa
            }
        });
    }
    
    /**
     * Refresh subscription từ API (force reload)
     */
    public void refreshSubscription(OnSubscriptionLoadedListener listener) {
        SessionManager sessionManager = new SessionManager(appContext);
        if (!sessionManager.isLoggedIn()) {
            if (listener != null) {
                listener.onSubscriptionLoaded(null);
            }
            return;
        }
        
        subscriptionService.getCurrentSubscription().enqueue(new Callback<CurrentSubscriptionResponse>() {
            @Override
            public void onResponse(Call<CurrentSubscriptionResponse> call, Response<CurrentSubscriptionResponse> response) {
                CurrentSubscriptionResponse subscription = null;
                if (response.isSuccessful() && response.body() != null) {
                    subscription = response.body();
                    saveSubscriptionToCache(subscription);
                } else {
                    saveSubscriptionToCache(null);
                }
                if (listener != null) {
                    listener.onSubscriptionLoaded(subscription);
                }
            }

            @Override
            public void onFailure(Call<CurrentSubscriptionResponse> call, Throwable t) {
                Log.e(TAG, "Error refreshing subscription: " + t.getMessage(), t);
                // Trả về cached subscription nếu có
                if (listener != null) {
                    listener.onSubscriptionLoaded(getCurrentSubscription());
                }
            }
        });
    }
    
    /**
     * Lấy subscription từ cache (không gọi API)
     */
    public CurrentSubscriptionResponse getCurrentSubscription() {
        return cachedSubscription;
    }
    
    /**
     * Clear subscription cache (khi logout)
     */
    public void clearSubscription() {
        saveSubscriptionToCache(null);
        Log.d(TAG, "Subscription cache cleared");
    }
    
    /**
     * Interface để listen khi subscription được load
     */
    public interface OnSubscriptionLoadedListener {
        void onSubscriptionLoaded(CurrentSubscriptionResponse subscription);
    }
}

