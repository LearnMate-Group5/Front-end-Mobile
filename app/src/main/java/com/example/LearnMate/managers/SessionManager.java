package com.example.LearnMate.managers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.LearnMate.view.WelcomeView;
import com.example.LearnMate.network.dto.LoginResponse;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context _context;
    private final Gson gson;


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void saveLoginSession(String token, LoginResponse.ValueData userValue) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_TOKEN, token);
        if (userValue != null) {
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

    public void logoutUser() {
        // Clear all data from Shared Preferences
        editor.clear();
        editor.apply();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, WelcomeView.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }
}
