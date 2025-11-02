package com.example.LearnMate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.payment.PayOSConstants;
import com.example.LearnMate.payment.PayOSPaymentHelper;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionActivity extends AppCompatActivity {
    
    private RecyclerView rvSubscriptionPlans;
    private BottomNavigationComponent bottomNavComponent;
    private PayOSPaymentHelper paymentHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d("SubscriptionActivity", "onCreate called");
            setContentView(R.layout.activity_subscription);
            Log.d("SubscriptionActivity", "Layout set");
            
            paymentHelper = new PayOSPaymentHelper(this);
            
            setupUI();
            loadSubscriptionPlans();
            
            Log.d("SubscriptionActivity", "Setup completed");
        } catch (Exception e) {
            Log.e("SubscriptionActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("SubscriptionActivity", "onStart called");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("SubscriptionActivity", "onResume called");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("SubscriptionActivity", "onPause called");
    }
    
    @Override
    public void onBackPressed() {
        Log.d("SubscriptionActivity", "onBackPressed called");
        super.onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("SubscriptionActivity", "onDestroy called");
    }
    
    private void setupUI() {
        try {
            // Back button
            View btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
                Log.d("SubscriptionActivity", "Back button setup");
            } else {
                Log.e("SubscriptionActivity", "btnBack is NULL!");
            }
            
            // Bottom navigation - Ẩn để tránh quay lại SettingsActivity
            bottomNavComponent = findViewById(R.id.bottomNavComponent);
            if (bottomNavComponent != null) {
                // Ẩn bottom nav trong SubscriptionActivity để tránh navigation conflict
                bottomNavComponent.setVisibility(View.GONE);
                Log.d("SubscriptionActivity", "Bottom nav hidden");
            } else {
                Log.w("SubscriptionActivity", "bottomNavComponent is NULL");
            }
            
            // RecyclerView cho subscription plans
            rvSubscriptionPlans = findViewById(R.id.rvSubscriptionPlans);
            if (rvSubscriptionPlans != null) {
                rvSubscriptionPlans.setLayoutManager(new LinearLayoutManager(this));
                Log.d("SubscriptionActivity", "RecyclerView setup");
            } else {
                Log.e("SubscriptionActivity", "rvSubscriptionPlans is NULL!");
            }
        } catch (Exception e) {
            Log.e("SubscriptionActivity", "Error in setupUI", e);
            e.printStackTrace();
        }
    }
    
    private void loadSubscriptionPlans() {
        // Lấy current subscription status
        SharedPreferences prefs = getSharedPreferences("subscription_prefs", MODE_PRIVATE);
        
        // TODO: Lấy từ backend API - user.isPremium
        // Tạm thời mock để test UI - có thể set true để test premium UI
        boolean isPremiumFromBackend = prefs.getBoolean("is_premium", false); // Mock: lấy từ backend
        
        String currentPlanType = prefs.getString("subscription_type", "FREE");
        boolean isPremium = isPremiumFromBackend || !currentPlanType.equals("FREE");
        
        Log.d("SubscriptionActivity", "Current plan type: " + currentPlanType);
        Log.d("SubscriptionActivity", "Is Premium (from backend): " + isPremiumFromBackend);
        Log.d("SubscriptionActivity", "Is Premium (final): " + isPremium);
        
        // Tạo danh sách 2 gói: Current Plan và Premium
        List<SubscriptionPlan> plans = new ArrayList<>();
        
        // Current Plan
        if (isPremium) {
            plans.add(new SubscriptionPlan(
                "Current Plan",
                0,
                "Premium Active",
                "• Unlimited PDF imports\n• Advanced AI features\n• Priority support\n• Cloud storage\n• Ad-free experience",
                currentPlanType,
                true // isCurrentPlan
            ));
        } else {
            plans.add(new SubscriptionPlan(
                "Current Plan",
                0,
                "Free Plan",
                "• 5 PDF imports per month\n• Basic AI features\n• Limited storage",
                "FREE",
                true // isCurrentPlan
            ));
        }
        
        // Premium Plan
        plans.add(new SubscriptionPlan(
            "Premium",
            99000, // 99,000 VND/month
            "Upgrade to unlock all features",
            "• Unlimited PDF imports\n• Advanced AI features\n• Priority support\n• Cloud storage\n• Ad-free experience\n• Early access to new features",
            "PREMIUM",
            false // isCurrentPlan
        ));
        
        // Setup adapter
        try {
            if (rvSubscriptionPlans != null) {
                SubscriptionAdapter adapter = new SubscriptionAdapter(plans, this::onPlanSelected);
                rvSubscriptionPlans.setAdapter(adapter);
                Log.d("SubscriptionActivity", "Loaded " + plans.size() + " subscription plans");
                Log.d("SubscriptionActivity", "Adapter set with " + adapter.getItemCount() + " items");
            } else {
                Log.e("SubscriptionActivity", "rvSubscriptionPlans is NULL, cannot set adapter!");
            }
        } catch (Exception e) {
            Log.e("SubscriptionActivity", "Error setting adapter", e);
            e.printStackTrace();
        }
    }
    
    private void onPlanSelected(SubscriptionPlan plan) {
        // Nếu là current plan thì không làm gì
        if (plan.isCurrentPlan()) {
            return;
        }
        
        // Bắt đầu thanh toán PayOS (không cần app riêng, mở trong browser/WebView)
        String description = "Thanh toán gói Premium - LearnMate";
        
        paymentCallback = new PayOSPaymentHelper.PaymentCallback() {
                @Override
                public void onPaymentLinkCreated(com.example.LearnMate.payment.dto.PayOSOrderResponse response) {
                    if (response.isSuccess() && response.getData() != null) {
                        // Mở PayOS payment page
                        String checkoutUrl = response.getData().getCheckoutUrl();
                        paymentHelper.openPaymentPage(SubscriptionActivity.this, checkoutUrl);
                    } else {
                        Toast.makeText(SubscriptionActivity.this,
                            "Lỗi: " + response.getDesc(),
                            Toast.LENGTH_LONG).show();
                    }
                }
                
                @Override
                public void onPaymentSuccess(String orderCode) {
                    Toast.makeText(SubscriptionActivity.this,
                        "Thanh toán thành công! Order Code: " + orderCode,
                        Toast.LENGTH_LONG).show();
                    // Update subscription status
                    updateSubscriptionStatus(plan);
                    
                    // TODO: Gọi backend API để update user.isPremium = true
                    // Ví dụ:
                    // ApiService service = RetrofitClient.getApiService();
                    // service.updateSubscriptionStatus(userId, true).enqueue(...);
                }
                
                @Override
                public void onPaymentFailed(String errorMessage) {
                    Toast.makeText(SubscriptionActivity.this,
                        "Thanh toán thất bại: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(SubscriptionActivity.this,
                        "Lỗi: " + error,
                        Toast.LENGTH_LONG).show();
                }
            };
        
        paymentHelper.pay(this, plan.getPrice(), description, paymentCallback);
    }
    
    
    private PayOSPaymentHelper.PaymentCallback paymentCallback;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PayOSConstants.REQUEST_CODE_PAYOS && paymentCallback != null) {
            paymentHelper.handlePaymentResult(requestCode, resultCode, data, paymentCallback);
        }
    }
    
    private void updateSubscriptionStatus(SubscriptionPlan plan) {
        // TODO: Gọi backend API để update user.isPremium = true
        // ApiService service = RetrofitClient.getApiService();
        // Call<ResponseBody> call = service.updateSubscription(userId, true);
        // call.enqueue(new Callback<ResponseBody>() { ... });
        
        // Save locally để hiển thị ngay
        SharedPreferences prefs = getSharedPreferences("subscription_prefs", MODE_PRIVATE);
        prefs.edit()
            .putString("subscription_type", plan.getType())
            .putBoolean("is_premium", true) // Mock: set isPremium = true
            .putLong("subscription_expiry", System.currentTimeMillis() + 
                (plan.getType().equals("MONTHLY") ? 30L * 24 * 60 * 60 * 1000 : 365L * 24 * 60 * 60 * 1000))
            .apply();
        
        Log.d("SubscriptionActivity", "Updated subscription status: " + plan.getType() + ", isPremium: true");
        
        Toast.makeText(this, "Gói " + plan.getName() + " đã được kích hoạt!", Toast.LENGTH_LONG).show();
        
        // Reload plans để hiển thị updated status
        loadSubscriptionPlans();
    }
    
    /**
     * Model class cho subscription plan
     */
    public static class SubscriptionPlan {
        private String name;
        private long price; // VND
        private String subtitle;
        private String features;
        private String type; // FREE, PREMIUM, etc.
        private boolean isCurrentPlan;
        
        public SubscriptionPlan(String name, long price, String subtitle, String features, String type, boolean isCurrentPlan) {
            this.name = name;
            this.price = price;
            this.subtitle = subtitle;
            this.features = features;
            this.type = type;
            this.isCurrentPlan = isCurrentPlan;
        }
        
        public String getName() { return name; }
        public long getPrice() { return price; }
        public String getSubtitle() { return subtitle; }
        public String getFeatures() { return features; }
        public String getType() { return type; }
        public boolean isCurrentPlan() { return isCurrentPlan; }
        
        public String getFormattedPrice() {
            if (price == 0) return isCurrentPlan ? "Active" : "Miễn phí";
            return String.format("%,d VND/tháng", price);
        }
    }
}

