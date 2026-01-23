package com.example.LearnMate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.managers.SubscriptionManager;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.example.LearnMate.network.dto.SubscriptionPlanResponse;
import com.example.LearnMate.SubscriptionAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionUpgradeActivity extends AppCompatActivity {
    
    private static final String TAG = "SubscriptionUpgrade";
    
    private RecyclerView rvSubscriptionPlans;
    private BottomNavigationComponent bottomNavComponent;
    private ProgressDialog progressDialog;
    private SubscriptionService subscriptionService;
    private CurrentSubscriptionResponse currentSubscription;
    private String currentSubscriptionId;
    private List<SubscriptionActivity.SubscriptionPlan> plansList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d(TAG, "onCreate called");
            setContentView(R.layout.activity_subscription);
            Log.d(TAG, "Layout set");
            
            // Get current subscription ID from intent
            currentSubscriptionId = getIntent().getStringExtra("currentSubscriptionId");
            Log.d(TAG, "Current subscription ID: " + currentSubscriptionId);
            
            // Initialize services
            subscriptionService = RetrofitClient.getSubscriptionService(this);
            
            setupUI();
            loadCurrentSubscription();
            
            Log.d(TAG, "Setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
        Log.d(TAG, "onDestroy called");
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
                Log.d(TAG, "Back button setup");
            }
            
            // Change title to "Nâng Cấp Gói"
            TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) {
                tvTitle.setText("Nâng Cấp Gói");
            }
            
            // Bottom navigation - Hide
            bottomNavComponent = findViewById(R.id.bottomNavComponent);
            if (bottomNavComponent != null) {
                bottomNavComponent.setVisibility(View.GONE);
                Log.d(TAG, "Bottom nav hidden");
            }
            
            // RecyclerView cho subscription plans
            rvSubscriptionPlans = findViewById(R.id.rvSubscriptionPlans);
            if (rvSubscriptionPlans != null) {
                rvSubscriptionPlans.setLayoutManager(new LinearLayoutManager(this));
                Log.d(TAG, "RecyclerView setup");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupUI", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Load current subscription từ API
     * GET /api/Subscription/plans/my/current
     */
    private void loadCurrentSubscription() {
        showLoadingDialog("Đang tải thông tin gói...");
        
        subscriptionService.getCurrentSubscription().enqueue(new Callback<CurrentSubscriptionResponse>() {
            @Override
            public void onResponse(Call<CurrentSubscriptionResponse> call, Response<CurrentSubscriptionResponse> response) {
                dismissLoadingDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    currentSubscription = response.body();
                    Log.d(TAG, "Current subscription loaded: " + currentSubscription.name);
                    
                    // Load danh sách plans sau khi có current subscription
                    loadSubscriptionPlans();
                } else {
                    Log.w(TAG, "No current subscription found - Response code: " + (response != null ? response.code() : "null"));
                    currentSubscription = null;
                    
                    // Vẫn load danh sách plans để user có thể chọn
                    loadSubscriptionPlans();
                }
            }

            @Override
            public void onFailure(Call<CurrentSubscriptionResponse> call, Throwable t) {
                dismissLoadingDialog();
                Log.e(TAG, "Error loading current subscription: " + t.getMessage(), t);
                currentSubscription = null;
                
                // Vẫn load danh sách plans
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
                    Log.d(TAG, "Loaded " + apiPlans.size() + " plans from API");
                    
                    plansList.clear();
                    
                    // Chỉ hiển thị các plans có giá cao hơn plan hiện tại (để upgrade)
                    for (SubscriptionPlanResponse apiPlan : apiPlans) {
                        // Skip plan hiện tại
                        if (currentSubscription != null && 
                            currentSubscription.subscriptionId != null &&
                            currentSubscription.subscriptionId.equals(apiPlan.subscriptionId)) {
                            continue;
                        }
                        
                        // Chỉ hiển thị plans có giá cao hơn
                        if (currentSubscription != null && 
                            apiPlan.getFinalPrice() <= currentSubscription.getFinalPrice()) {
                            continue;
                        }
                        
                        SubscriptionActivity.SubscriptionPlan plan = new SubscriptionActivity.SubscriptionPlan(
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
                    
                    // Setup adapter với mode upgrade
                    updateAdapter(true);
                } else {
                    Log.e(TAG, "Failed to load plans: " + response.code());
                    Toast.makeText(SubscriptionUpgradeActivity.this, "Không thể tải danh sách gói đăng ký", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionPlanResponse>> call, Throwable t) {
                dismissLoadingDialog();
                Log.e(TAG, "Error loading plans: " + t.getMessage());
                Toast.makeText(SubscriptionUpgradeActivity.this, "Lỗi khi tải gói đăng ký: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Update adapter với mode upgrade
     */
    private void updateAdapter(boolean isUpgradeMode) {
        SubscriptionAdapter adapter = new SubscriptionAdapter(plansList, plan -> {
            upgradeToPlan(plan);
        }, null); // No cancel handler for upgrade
        rvSubscriptionPlans.setAdapter(adapter);
    }
    
    /**
     * Upgrade to selected plan
     * POST /api/Subscription/plans/{subscriptionId}/upgrade
     */
    private void upgradeToPlan(SubscriptionActivity.SubscriptionPlan plan) {
        if (plan.getSubscriptionId() == null || plan.getSubscriptionId().isEmpty()) {
            Toast.makeText(this, "Không thể nâng cấp. Thiếu thông tin gói.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoadingDialog("Đang nâng cấp gói...");
        
        subscriptionService.upgradePlan(plan.getSubscriptionId()).enqueue(new Callback<CurrentSubscriptionResponse>() {
            @Override
            public void onResponse(Call<CurrentSubscriptionResponse> call, Response<CurrentSubscriptionResponse> response) {
                dismissLoadingDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    CurrentSubscriptionResponse upgradedSubscription = response.body();
                    Log.d(TAG, "Upgrade successful: " + upgradedSubscription.name);
                    
                    // Refresh subscription cache
                    SubscriptionManager.getInstance(SubscriptionUpgradeActivity.this).loadSubscriptionFromAPI();
                    
                    Toast.makeText(SubscriptionUpgradeActivity.this, "Nâng cấp gói thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Return to Settings
                    Intent intent = new Intent();
                    intent.putExtra("upgrade_success", true);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    // Handle error response
                    String errorMessage = "Không thể nâng cấp gói";
                    
                    try {
                        // Try to parse error message from response body
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Upgrade error response: " + errorBody);
                            
                            // Try to extract message from JSON
                            if (errorBody.contains("\"message\"")) {
                                int messageStart = errorBody.indexOf("\"message\"") + 10;
                                int messageEnd = errorBody.indexOf("\"", messageStart);
                                if (messageEnd > messageStart) {
                                    errorMessage = errorBody.substring(messageStart, messageEnd);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                    }
                    
                    Toast.makeText(SubscriptionUpgradeActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Upgrade failed: " + response.code() + " - " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CurrentSubscriptionResponse> call, Throwable t) {
                dismissLoadingDialog();
                String errorMsg = "Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "Không xác định");
                Toast.makeText(SubscriptionUpgradeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error upgrading plan", t);
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
            return "• Nhập PDF cơ bản\n• Tính năng AI cơ bản\n• Hỗ trợ tiêu chuẩn\n• Lưu trữ giới hạn";
        } else if (type.contains("basic")) {
            return "• Nhập PDF cơ bản\n• Tính năng cơ bản\n• Hỗ trợ cộng đồng";
        }
        return "";
    }
    
    /**
     * Translate plan type to Vietnamese
     */
    private String translateType(String type) {
        if (type == null) return "";
        
        type = type.toLowerCase();
        if (type.contains("premium")) {
            return "Premium";
        } else if (type.contains("standard")) {
            return "Standard";
        } else if (type.contains("basic")) {
            return "Basic";
        }
        return type;
    }
}

