package com.example.LearnMate;

import android.app.Application;
import android.util.Log;

import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.managers.SubscriptionManager;
import com.example.LearnMate.managers.UserManager;

/**
 * Application class
 */
public class LearnMateApplication extends Application {
    
    private static final String TAG = "LearnMateApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");
        
        // Load subscription và user profile ngay khi app khởi động nếu user đã login
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User is logged in, loading subscription and user profile...");
            SubscriptionManager.getInstance(this).loadSubscriptionFromAPI();
            UserManager.getInstance(this).loadUserProfileFromAPI();
        }
    }
}
