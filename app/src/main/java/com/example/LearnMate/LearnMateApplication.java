package com.example.LearnMate;

import android.app.Application;
import android.util.Log;

/**
 * Application class
 */
public class LearnMateApplication extends Application {
    
    private static final String TAG = "LearnMateApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");
    }
}
