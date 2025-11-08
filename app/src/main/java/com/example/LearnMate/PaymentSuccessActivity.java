package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.managers.SubscriptionManager;
import com.google.android.material.button.MaterialButton;

public class PaymentSuccessActivity extends AppCompatActivity {

    private static final String TAG = "PaymentSuccessActivity";
    
    private ImageView imageSuccessIcon;
    private TextView textSuccessTitle;
    private TextView textSuccessMessage;
    private MaterialButton buttonBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        initViews();
        setupClickListeners();
        
        // Refresh subscription ngay khi màn hình này được mở (sau khi thanh toán thành công)
        refreshSubscription();
    }

    private void initViews() {
        imageSuccessIcon = findViewById(R.id.imageSuccessIcon);
        textSuccessTitle = findViewById(R.id.textSuccessTitle);
        textSuccessMessage = findViewById(R.id.textSuccessMessage);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
    }

    private void setupClickListeners() {
        buttonBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Refresh subscription trước khi chuyển về Home để đảm bảo dữ liệu mới nhất
                refreshSubscriptionAndNavigate();
            }
        });
    }
    
    /**
     * Refresh subscription từ API
     */
    private void refreshSubscription() {
        SubscriptionManager subscriptionManager = SubscriptionManager.getInstance(this);
        subscriptionManager.loadSubscriptionFromAPI();
        Log.d(TAG, "Subscription refreshed from API");
    }
    
    /**
     * Refresh subscription và navigate về Home
     */
    private void refreshSubscriptionAndNavigate() {
        SubscriptionManager subscriptionManager = SubscriptionManager.getInstance(this);
        
        // Refresh subscription từ API trước khi navigate
        subscriptionManager.refreshSubscription(subscription -> {
            Log.d(TAG, "Subscription refreshed before navigating to Home");
            
            // Chuyển về màn hình Home với flag để báo cần refresh
            Intent intent = new Intent(PaymentSuccessActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("refresh_subscription", true);
            startActivity(intent);
            finish();
        });
    }
}

