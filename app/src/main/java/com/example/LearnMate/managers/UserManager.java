package com.example.LearnMate.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.UserRolesMeResponse;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager để quản lý thông tin user profile
 * Tự động load profile khi có token và cache vào SharedPreferences
 */
public class UserManager {
    private static final String TAG = "UserManager";
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_PROFILE = "user_profile";
    
    private final Context appContext;
    private final SharedPreferences pref;
    private final AuthService authService;
    private final Gson gson;
    
    private static UserManager instance;
    private UserRolesMeResponse cachedUserProfile;
    
    private UserManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.authService = RetrofitClient.getAuthService(appContext);
        this.gson = new Gson();
        loadUserProfileFromCache();
    }
    
    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }
    
    /**
     * Load user profile từ cache (SharedPreferences)
     */
    private void loadUserProfileFromCache() {
        String profileJson = pref.getString(KEY_USER_PROFILE, null);
        if (profileJson != null) {
            try {
                cachedUserProfile = gson.fromJson(profileJson, UserRolesMeResponse.class);
                Log.d(TAG, "Loaded user profile from cache: " + (cachedUserProfile != null ? cachedUserProfile.name : "null"));
            } catch (Exception e) {
                Log.e(TAG, "Error loading user profile from cache", e);
                cachedUserProfile = null;
            }
        }
    }
    
    /**
     * Lưu user profile vào cache
     */
    private void saveUserProfileToCache(UserRolesMeResponse userProfile) {
        if (userProfile != null) {
            String profileJson = gson.toJson(userProfile);
            pref.edit().putString(KEY_USER_PROFILE, profileJson).apply();
            cachedUserProfile = userProfile;
            Log.d(TAG, "Saved user profile to cache: " + userProfile.name);
        } else {
            pref.edit().remove(KEY_USER_PROFILE).apply();
            cachedUserProfile = null;
            Log.d(TAG, "Removed user profile from cache");
        }
    }
    
    /**
     * Xóa user profile khỏi cache
     */
    public void clearUserProfile() {
        pref.edit().remove(KEY_USER_PROFILE).apply();
        cachedUserProfile = null;
        Log.d(TAG, "Cleared user profile from cache");
    }
    
    /**
     * Lấy user profile từ cache
     */
    public UserRolesMeResponse getUserProfile() {
        return cachedUserProfile;
    }
    
    /**
     * Load user profile từ API và lưu vào cache
     */
    public void loadUserProfileFromAPI() {
        Log.d(TAG, "Fetching user profile from API...");
        authService.getUserRolesMe().enqueue(new Callback<UserRolesMeResponse>() {
            @Override
            public void onResponse(Call<UserRolesMeResponse> call, Response<UserRolesMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRolesMeResponse userProfile = response.body();
                    saveUserProfileToCache(userProfile);
                    Log.d(TAG, "API fetch successful: " + userProfile.name);
                } else {
                    Log.w(TAG, "API fetch: No user profile found or empty response. Code: " + (response != null ? response.code() : "null"));
                    clearUserProfile();
                }
            }

            @Override
            public void onFailure(Call<UserRolesMeResponse> call, Throwable t) {
                Log.e(TAG, "API fetch failed: " + t.getMessage(), t);
                clearUserProfile(); // Clear cache on API failure to ensure fresh data next time
            }
        });
    }
    
    /**
     * Refresh user profile từ API với callback
     */
    public interface UserProfileLoadCallback {
        void onUserProfileLoaded(UserRolesMeResponse userProfile);
    }
    
    public void refreshUserProfile(UserProfileLoadCallback callback) {
        Log.d(TAG, "Refreshing user profile from API with callback...");
        authService.getUserRolesMe().enqueue(new Callback<UserRolesMeResponse>() {
            @Override
            public void onResponse(Call<UserRolesMeResponse> call, Response<UserRolesMeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserRolesMeResponse userProfile = response.body();
                    saveUserProfileToCache(userProfile);
                    Log.d(TAG, "API refresh successful: " + userProfile.name);
                    callback.onUserProfileLoaded(userProfile);
                } else {
                    Log.w(TAG, "API refresh: No user profile found or empty response. Code: " + (response != null ? response.code() : "null"));
                    clearUserProfile();
                    callback.onUserProfileLoaded(null);
                }
            }

            @Override
            public void onFailure(Call<UserRolesMeResponse> call, Throwable t) {
                Log.e(TAG, "API refresh failed: " + t.getMessage(), t);
                clearUserProfile();
                callback.onUserProfileLoaded(null);
            }
        });
    }
}

