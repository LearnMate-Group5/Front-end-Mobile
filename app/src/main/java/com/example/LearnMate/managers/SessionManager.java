package com.example.LearnMate.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.LearnMate.WelcomeActivity;
import com.example.LearnMate.network.dto.LoginResponse;
import com.google.gson.Gson;

public class SessionManager {
    // SharedPreferences file & keys
    private static final String PREF_NAME = "user_prefs";

    // Keys “mới” bạn đang dùng trong class này
    private static final String KEY_USER_TOKEN   = "user_token";
    private static final String KEY_USER_DATA    = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // (Tuỳ chọn) Các keys “cũ” từng lưu ở nơi khác – xoá cho sạch
    private static final String LEGACY_TOKEN        = "token";
    private static final String LEGACY_REFRESH      = "refresh_token";
    private static final String LEGACY_EXPIRES_AT   = "expires_at";
    private static final String LEGACY_USER_ID      = "user_id";
    private static final String LEGACY_USER_NAME    = "user_name";
    private static final String LEGACY_USER_EMAIL   = "user_email";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context appContext; // giữ appContext để dùng an toàn
    private final Gson gson;

    public SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
        this.gson = new Gson();
    }

    /** Lưu token + thông tin user (tuỳ theo DTO LoginResponse bạn đang dùng) */
    public void saveLoginSession(String token, LoginResponse.ValueData userValue) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_TOKEN, token);

        if (userValue != null && userValue.getUser() != null) {
            String userJson = gson.toJson(userValue.getUser());
            editor.putString(KEY_USER_DATA, userJson);
        }
        editor.apply();
    }

    public String getToken() {
        return pref.getString(KEY_USER_TOKEN, null);
    }

    public LoginResponse.UserData getUserData() {
        String userJson = pref.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            return gson.fromJson(userJson, LoginResponse.UserData.class);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /** Đăng xuất: xoá toàn bộ session và quay về WelcomeActivity (clear back stack) */
    public void logout(Activity activity) {
        // Xoá các key “mới”
        editor.remove(KEY_USER_TOKEN);
        editor.remove(KEY_USER_DATA);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);

        // Xoá các key “cũ” (nếu từng dùng)
        editor.remove(LEGACY_TOKEN);
        editor.remove(LEGACY_REFRESH);
        editor.remove(LEGACY_EXPIRES_AT);
        editor.remove(LEGACY_USER_ID);
        editor.remove(LEGACY_USER_NAME);
        editor.remove(LEGACY_USER_EMAIL);

        editor.apply();

        // Điều hướng về màn Welcome và xoá toàn bộ back stack
        Intent i = new Intent(activity, WelcomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
