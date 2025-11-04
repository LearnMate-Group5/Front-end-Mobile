package com.example.LearnMate;

import android.app.Application;
import android.util.Log;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

/**
 * Application class để khởi tạo ZaloPay SDK
 */
public class LearnMateApplication extends Application {
    
    private static final String TAG = "LearnMateApplication";
    
    // ZaloPay Configuration - Demo AppID
    private static final int ZALOPAY_APP_ID = 554;
    private static final Environment ZALOPAY_ENVIRONMENT = Environment.SANDBOX; // Sử dụng SANDBOX cho demo
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            Log.d(TAG, "=== Initializing ZaloPay SDK ===");
            Log.d(TAG, "AppID: " + ZALOPAY_APP_ID);
            Log.d(TAG, "Environment: " + ZALOPAY_ENVIRONMENT);
            
            // Khởi tạo ZaloPay SDK
            ZaloPaySDK.init(ZALOPAY_APP_ID, ZALOPAY_ENVIRONMENT);
            
            // Verify SDK initialization
            if (ZaloPaySDK.getInstance() != null) {
                Log.d(TAG, "✓ ZaloPay SDK initialized successfully");
                Log.d(TAG, "SDK Instance: " + ZaloPaySDK.getInstance().getClass().getName());
            } else {
                Log.e(TAG, "✗ ZaloPay SDK instance is null after initialization!");
            }
        } catch (Exception e) {
            Log.e(TAG, "✗ Error initializing ZaloPay SDK", e);
            e.printStackTrace();
        }
    }
}
