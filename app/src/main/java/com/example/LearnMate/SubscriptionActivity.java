package com.example.LearnMate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.dto.ChoosePlanResponse;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.example.LearnMate.network.dto.SubscriptionPlanResponse;
import com.example.LearnMate.payment.PayOSConstants;
import com.example.LearnMate.payment.PayOSPaymentHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionActivity extends AppCompatActivity {
    
    private RecyclerView rvSubscriptionPlans;
    private BottomNavigationComponent bottomNavComponent;
    private PayOSPaymentHelper payOSPaymentHelper;
    private ProgressDialog progressDialog;
    private SubscriptionService subscriptionService;
    private CurrentSubscriptionResponse currentSubscription;
    private List<SubscriptionPlan> plansList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d("SubscriptionActivity", "onCreate called");
            setContentView(R.layout.activity_subscription);
            Log.d("SubscriptionActivity", "Layout set");
            
            // Initialize services
            subscriptionService = RetrofitClient.getSubscriptionService(this);
            payOSPaymentHelper = new PayOSPaymentHelper(this);
            
            setupUI();
            loadCurrentSubscription();
            
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
        dismissLoadingDialog();
        Log.d("SubscriptionActivity", "onDestroy called");
    }
    
    private void showLoadingDialog(String message) {
        dismissLoadingDialog();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    
    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
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
    
    /**
     * Load current subscription từ API
     * GET /api/Subscription/plans/my/current
     */
    private void loadCurrentSubscription() {
        showLoadingDialog("Đang tải đăng ký...");
        
        subscriptionService.getCurrentSubscription().enqueue(new Callback<CurrentSubscriptionResponse>() {
            @Override
            public void onResponse(Call<CurrentSubscriptionResponse> call, Response<CurrentSubscriptionResponse> response) {
                dismissLoadingDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    currentSubscription = response.body();
                    Log.d("SubscriptionActivity", "Current subscription loaded: " + currentSubscription.name);
                    
                    // Load danh sách plans sau khi có current subscription
                    loadSubscriptionPlans();
                } else {
                    Log.w("SubscriptionActivity", "No current subscription found - Response code: " + (response != null ? response.code() : "null"));
                    currentSubscription = null;
                    
                    // Vẫn load danh sách plans để user có thể chọn
                    loadSubscriptionPlans();
                }
            }

            @Override
            public void onFailure(Call<CurrentSubscriptionResponse> call, Throwable t) {
                dismissLoadingDialog();
                Log.e("SubscriptionActivity", "Error loading current subscription: " + t.getMessage(), t);
                currentSubscription = null;
                
                // Hiển thị "Current Plan is Empty" và vẫn load danh sách plans
                loadSubscriptionPlans();
            }
        });
    }
    
    /**
     * Load danh sách subscription plans từ API
     * GET /api/Subscription/plans
     */
    private void loadSubscriptionPlans() {
        showLoadingDialog("Đang tải gói đăng ký...");
        
        subscriptionService.getPlans().enqueue(new Callback<List<SubscriptionPlanResponse>>() {
            @Override
            public void onResponse(Call<List<SubscriptionPlanResponse>> call, Response<List<SubscriptionPlanResponse>> response) {
                dismissLoadingDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    List<SubscriptionPlanResponse> apiPlans = response.body();
                    Log.d("SubscriptionActivity", "Loaded " + apiPlans.size() + " plans from API");
                    
                    plansList.clear();
                    
                    // Chỉ thêm các plans từ API (không thêm Current Plan nữa vì đã chuyển sang Settings)
                    for (SubscriptionPlanResponse apiPlan : apiPlans) {
                        SubscriptionPlan plan = new SubscriptionPlan(
                            apiPlan.name,
                            apiPlan.getFinalPrice(),
                            apiPlan.originalPrice,
                            apiPlan.discount,
                            "Loại: " + translateType(apiPlan.type),
                            buildFeaturesFromType(apiPlan.type),
                            apiPlan.type,
                            false,
                            apiPlan.subscriptionId
                        );
                        plansList.add(plan);
                    }
                    
                    // Setup adapter
                    updateAdapter();
                } else {
                    Log.e("SubscriptionActivity", "Failed to load plans: " + response.code());
                    Toast.makeText(SubscriptionActivity.this, "Không thể tải danh sách gói đăng ký", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionPlanResponse>> call, Throwable t) {
                dismissLoadingDialog();
                Log.e("SubscriptionActivity", "Error loading plans: " + t.getMessage());
                Toast.makeText(SubscriptionActivity.this, "Lỗi khi tải gói đăng ký: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Build features string từ plan type (tiếng Việt)
     */
    private String buildFeaturesFromType(String type) {
        if (type == null) return "";
        
        type = type.toLowerCase();
        if (type.contains("premium")) {
            return "• Nhập PDF không giới hạn\n• Tính năng AI nâng cao\n• Hỗ trợ ưu tiên\n• Lưu trữ đám mây\n• Trải nghiệm không quảng cáo\n• Truy cập sớm các tính năng mới";
        } else if (type.contains("standard")) {
            return "• 20 lần nhập PDF mỗi tháng\n• Tính năng AI tiêu chuẩn\n• Hỗ trợ qua email\n• Lưu trữ đám mây giới hạn";
        } else {
            return "• 5 lần nhập PDF mỗi tháng\n• Tính năng AI cơ bản\n• Lưu trữ giới hạn";
        }
    }
    
    /**
     * Dịch type sang tiếng Việt
     */
    private String translateType(String type) {
        if (type == null) return "";
        
        type = type.toLowerCase();
        if (type.contains("premium")) {
            return "Cao cấp";
        } else if (type.contains("standard")) {
            return "Tiêu chuẩn";
        } else {
            return "Miễn phí";
        }
    }
    
    /**
     * Update adapter với danh sách plans
     */
    private void updateAdapter() {
        try {
            if (rvSubscriptionPlans != null) {
                SubscriptionAdapter adapter = new SubscriptionAdapter(plansList, this::onPlanSelected, this::onCancelClicked);
                rvSubscriptionPlans.setAdapter(adapter);
                Log.d("SubscriptionActivity", "Adapter updated with " + plansList.size() + " plans");
            } else {
                Log.e("SubscriptionActivity", "rvSubscriptionPlans is NULL, cannot set adapter!");
            }
        } catch (Exception e) {
            Log.e("SubscriptionActivity", "Error setting adapter", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Handle khi user chọn plan
     */
    private void onPlanSelected(SubscriptionPlan plan) {
        // Nếu là current plan thì không làm gì
        if (plan.isCurrentPlan()) {
            Log.d("SubscriptionActivity", "Plan is current plan, skipping");
            return;
        }
        
        if (plan.getSubscriptionId() == null) {
            Toast.makeText(this, "Gói không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("SubscriptionActivity", "=== Plan selected ===");
        Log.d("SubscriptionActivity", "Plan: " + plan.getName());
        Log.d("SubscriptionActivity", "SubscriptionId: " + plan.getSubscriptionId());
        
        // Hiển thị dialog với thông tin gói và button Pay
        showPaymentDialog(plan);
    }
    
    /**
     * Chuyển sang Activity payment với thông tin gói đã chọn
     */
    private void showPaymentDialog(SubscriptionPlan plan) {
        // Tìm current plan từ plansList
        SubscriptionPlan currentPlan = null;
        for (SubscriptionPlan p : plansList) {
            if (p.isCurrentPlan()) {
                currentPlan = p;
                break;
            }
        }
        
        // Nếu không tìm thấy current plan, tạo một plan Free mặc định
        if (currentPlan == null) {
            currentPlan = new SubscriptionPlan(
                "Free", 0, 0, 0,
                "", "", "FREE", true, null
            );
        }
        
        // Start SubscriptionPaymentActivity
        SubscriptionPaymentActivity.start(this, plan, currentPlan);
    }
    
    /**
     * Call API để choose plan
     * POST /api/Subscription/plans/{subscriptionId}/choose
     */
    private void choosePlan(String subscriptionId) {
        showLoadingDialog("Đang đăng ký gói...");
        
        subscriptionService.choosePlan(subscriptionId).enqueue(new Callback<ChoosePlanResponse>() {
            @Override
            public void onResponse(Call<ChoosePlanResponse> call, Response<ChoosePlanResponse> response) {
                dismissLoadingDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    ChoosePlanResponse result = response.body();
                    Log.d("SubscriptionActivity", "Plan chosen successfully: " + result.name);
                    
                    Toast.makeText(SubscriptionActivity.this, "Đăng ký thành công gói " + result.name + "!", Toast.LENGTH_LONG).show();
                    
                    // Refresh subscription trong SubscriptionManager để cache lại
                    com.example.LearnMate.managers.SubscriptionManager.getInstance(SubscriptionActivity.this).loadSubscriptionFromAPI();
                    
                    // Reload current subscription và plans
                    loadCurrentSubscription();
                } else {
                    Log.e("SubscriptionActivity", "Failed to choose plan: " + response.code());
                    Toast.makeText(SubscriptionActivity.this, "Không thể đăng ký gói", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChoosePlanResponse> call, Throwable t) {
                dismissLoadingDialog();
                Log.e("SubscriptionActivity", "Error choosing plan: " + t.getMessage());
                Toast.makeText(SubscriptionActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Handle khi user click Cancel button
     */
    private void onCancelClicked(SubscriptionPlan plan) {
        if (!plan.isCurrentPlan() || plan.getSubscriptionId() == null) {
            return;
        }
        
        // Hiển thị popup confirmation
        new AlertDialog.Builder(this)
            .setTitle("Hủy Đăng Ký")
            .setMessage("Bạn có chắc chắn muốn hủy đăng ký hiện tại?")
            .setPositiveButton("Có, Hủy", (dialog, which) -> {
                cancelSubscription();
            })
            .setNegativeButton("Không", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }
    
    /**
     * Call API để cancel subscription
     * POST /api/Subscription/plans/cancel
     */
    private void cancelSubscription() {
        showLoadingDialog("Đang hủy đăng ký...");
        
        subscriptionService.cancelSubscription().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                dismissLoadingDialog();
                
                if (response.isSuccessful()) {
                    Log.d("SubscriptionActivity", "Subscription cancelled successfully");
                    Toast.makeText(SubscriptionActivity.this, "Hủy đăng ký thành công", Toast.LENGTH_LONG).show();
                    
                    // Refresh subscription trong SubscriptionManager để cache lại
                    com.example.LearnMate.managers.SubscriptionManager.getInstance(SubscriptionActivity.this).loadSubscriptionFromAPI();
                    
                    // Reload current subscription và plans
                    loadCurrentSubscription();
                } else {
                    Log.e("SubscriptionActivity", "Failed to cancel subscription: " + response.code());
                    Toast.makeText(SubscriptionActivity.this, "Không thể hủy đăng ký", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                dismissLoadingDialog();
                Log.e("SubscriptionActivity", "Error cancelling subscription: " + t.getMessage());
                Toast.makeText(SubscriptionActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle PayOS payment result (nếu vẫn cần)
        if (requestCode == PayOSConstants.REQUEST_CODE_PAYOS && payOSPaymentHelper != null) {
            // PayOS payment handling if needed
        }
    }
    
    /**
     * Model class cho subscription plan
     */
    public static class SubscriptionPlan {
        private String name;
        private long price; // VND - giá sau discount
        private long originalPrice; // VND - giá gốc
        private int discount; // Phần trăm discount (0-100)
        private String subtitle;
        private String features;
        private String type; // FREE, PREMIUM, etc.
        private boolean isCurrentPlan;
        private String subscriptionId; // UUID của subscription plan
        
        public SubscriptionPlan(String name, long price, long originalPrice, int discount, String subtitle, String features, String type, boolean isCurrentPlan, String subscriptionId) {
            this.name = name;
            this.price = price;
            this.originalPrice = originalPrice;
            this.discount = discount;
            this.subtitle = subtitle;
            this.features = features;
            this.type = type;
            this.isCurrentPlan = isCurrentPlan;
            this.subscriptionId = subscriptionId;
        }
        
        public String getName() { return name; }
        public long getPrice() { return price; }
        public long getOriginalPrice() { return originalPrice; }
        public int getDiscount() { return discount; }
        public String getSubtitle() { return subtitle; }
        public String getFeatures() { return features; }
        public String getType() { return type; }
        public boolean isCurrentPlan() { return isCurrentPlan; }
        public String getSubscriptionId() { return subscriptionId; }
        
        public String getFormattedPrice() {
            if (price == 0) return isCurrentPlan ? "Hoạt Động" : "Miễn Phí";
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            return formatter.format(price) + " VND";
        }
        
        public String getFormattedOriginalPrice() {
            if (originalPrice == 0) return "";
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            return formatter.format(originalPrice) + " VND";
        }
        
        public String getFormattedDiscount() {
            if (discount <= 0) return "";
            return "-" + discount + "%";
        }
        
        public boolean hasDiscount() {
            return discount > 0 && originalPrice > 0;
        }
    }
}
