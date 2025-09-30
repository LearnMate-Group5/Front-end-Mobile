package com.example.learnmate_frontend.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.learnmate_frontend.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
public class SharedPrefManager {
    private static final String TAG = "SharedPrefManager";
    private static SharedPrefManager instance;
    private static final String PREF_NAME = "learnmate_shared_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";
    private static final String KEY_ROLE = "role";

    private SharedPreferences prefs;
    private Gson gson;

    private SharedPrefManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            if (context == null) {
                Log.e(TAG, "Context is null, cannot initialize SharedPrefManager");
                return null;
            }
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public boolean saveToken(String token) {
        if (token == null) {
            Log.w(TAG, "Attempted to save null token");
            return false;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        boolean success = editor.commit();
        if (!success) {
            Log.e(TAG, "Failed to save token");
        } else {
            Log.d(TAG, "Token saved successfully");
        }
        return success;
    }

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (token == null) {
            Log.w(TAG, "No token found");
        } else {
            Log.d(TAG, "Token retrieved: " + token.substring(0, Math.min(token.length(), 20)) + "...");
        }
        return token;
    }

    public boolean saveUser(User user) {
        if (user == null) {
            Log.w(TAG, "Attempted to save null user");
            return false;
        }
        String json = gson.toJson(user);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER, json);
        int userId = user.getId(); // Giả định getId() trả về int
        editor.putInt("user_id", userId); // Luôn lưu, không kiểm tra >= 0
        boolean success = editor.commit();
        if (!success) {
            Log.e(TAG, "Failed to save user");
        } else {
            Log.d(TAG, "User saved successfully, userId: " + userId);
        }
        return success;
    }

    public User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) {
            Log.w(TAG, "No user data found");
            return null;
        }
        try {
            User user = gson.fromJson(json, User.class);
            if (user == null) {
                Log.e(TAG, "Parsed user is null, clearing invalid data");
                prefs.edit().remove(KEY_USER).commit();
            } else {
                Log.d(TAG, "User retrieved: " + (user.getEmail() != null ? user.getEmail() : "null"));
            }
            return user;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Failed to parse user JSON: " + e.getMessage());
            prefs.edit().remove(KEY_USER).commit(); // Xóa dữ liệu lỗi
            return null;
        }
    }

    public int getUserId() {
        User user = getUser();
        if (user != null && user.getId() > 0) {
            Log.d(TAG, "UserId retrieved from User: " + user.getId());
            return user.getId();
        }
        int userId = prefs.getInt("user_id", -1);
        Log.d(TAG, "UserId retrieved from prefs: " + userId);
        return userId;
    }

    public int getRoleId() {
        User user = getUser();
        if (user != null) {
            int roleId = user.getRoleId();
            Log.d(TAG, "RoleId from User: " + roleId + ", User JSON: " + gson.toJson(user));
            return roleId;
        }
        Log.w(TAG, "No user found, returning 0");
        return 0;
    }
    public boolean saveUserId(int userId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", userId);
        boolean success = editor.commit();
        if (!success) {
            Log.e(TAG, "Failed to save userId");
        } else {
            Log.d(TAG, "UserId saved successfully: " + userId);
        }
        return success;
    }

    public boolean saveRole(String role) {
        if (role == null) {
            Log.w(TAG, "Attempted to save null role");
            return false;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ROLE, role);
        boolean success = editor.commit();
        if (!success) {
            Log.e(TAG, "Failed to save role");
        } else {
            Log.d(TAG, "Role saved successfully: " + role);
        }
        return success;
    }

    public String getRole() {
        String role = prefs.getString(KEY_ROLE, null);
        if (role == null) {
            Log.w(TAG, "No role found");
        } else {
            Log.d(TAG, "Role retrieved: " + role);
        }
        return role;
    }

    public boolean saveAll(String token, User user, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        if (token != null) editor.putString(KEY_TOKEN, token);
        if (user != null) {
            editor.putString(KEY_USER, gson.toJson(user));
            int userId = user.getId(); // Giả định getId() trả về int
            editor.putInt("user_id", userId); // Luôn lưu
        }
        if (role != null) editor.putString(KEY_ROLE, role);
        boolean success = editor.commit();
        if (!success) {
            Log.e(TAG, "Failed to save all data");
        } else {
            Log.d(TAG, "All data saved successfully");
        }
        return success;
    }

    public void clear() {
        prefs.edit().clear().commit();
        Log.d(TAG, "SharedPreferences cleared");
    }

    public boolean isInitialized() {
        return prefs != null;
    }
}
