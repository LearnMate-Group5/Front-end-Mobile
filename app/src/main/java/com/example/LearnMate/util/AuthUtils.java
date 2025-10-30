package com.example.LearnMate.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthUtils {
    
    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getBoolean("is_logged_in", false);
    }
    
    public static String getAccessToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getString("token", null);
    }
    
    public static String getUserEmail(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getString("user_email", null);
    }
    
    public static String getUserName(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getString("user_name", null);
    }
    
    public static String getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getString("user_id", null);
    }
    
    public static boolean isFirebaseLogin(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getBoolean("is_firebase_login", false);
    }
    
    public static void clearUserData(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
    
    public static void logout(Context context) {
        clearUserData(context);
        // Nếu có FirebaseAuthManager, có thể gọi signOut() ở đây
    }
}
