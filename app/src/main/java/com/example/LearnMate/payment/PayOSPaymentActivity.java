package com.example.LearnMate.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity để handle PayOS deep link return
 * PayOS sẽ redirect về: learnmate://payment/return?status=PAID&orderCode=123
 */
public class PayOSPaymentActivity extends AppCompatActivity {
    
    private static final String TAG = "PayOSPaymentActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Uri data = getIntent().getData();
        if (data != null) {
            String status = data.getQueryParameter("status");
            String orderCode = data.getQueryParameter("orderCode");
            
            Log.d(TAG, "PayOS return - Status: " + status + ", OrderCode: " + orderCode);
            
            // Tạo intent với result để gửi về SubscriptionActivity
            Intent resultIntent = new Intent();
            resultIntent.setData(data);
            
            if ("PAID".equals(status) || "success".equals(status)) {
                setResult(RESULT_OK, resultIntent);
            } else {
                setResult(RESULT_CANCELED, resultIntent);
            }
        }
        
        finish();
    }
}




