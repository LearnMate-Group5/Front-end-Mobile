package com.example.LearnMate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;

import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.managers.SubscriptionManager;
import com.example.LearnMate.managers.UserManager;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.example.LearnMate.network.dto.UserRolesMeResponse;

import androidx.appcompat.app.AlertDialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserEmail;
    private ImageView imgAvatar;
    private MaterialButton btnShowProfile;
    private ImageView btnSettings;
    private LinearLayout btnLogout;
    
    private TextView tvCurrentPlanTitle;
    private TextView tvCurrentPlanBadge;
    private TextView tvCurrentPlanStatus;
    private TextView tvCurrentPlanName;
    private TextView tvCurrentPlanFeatures;
    private MaterialButton btnUpgrade;
    private MaterialButton btnCancel;

    private SessionManager sessionManager;
    private BottomNavigationComponent bottomNavComponent;
    private AuthService authService;
    private UserManager userManager;
    private SubscriptionService subscriptionService;
    private CurrentSubscriptionResponse currentSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);
        authService = RetrofitClient.getAuthService(this);
        subscriptionService = RetrofitClient.getSubscriptionService(this);
        userManager = UserManager.getInstance(this);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnShowProfile = findViewById(R.id.btnShowProfile);
        // btnSettings = findViewById(R.id.btnSettings); // This ID does not exist in
        // the layout
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        btnLogout = findViewById(R.id.btnLogout);
        
        tvCurrentPlanTitle = findViewById(R.id.tvCurrentPlanTitle);
        tvCurrentPlanBadge = findViewById(R.id.tvCurrentPlanBadge);
        tvCurrentPlanStatus = findViewById(R.id.tvCurrentPlanStatus);
        tvCurrentPlanName = findViewById(R.id.tvCurrentPlanName);
        tvCurrentPlanFeatures = findViewById(R.id.tvCurrentPlanFeatures);
        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnCancel = findViewById(R.id.btnCancel);

        btnLogout.setOnClickListener(v -> sessionManager.logout(SettingsActivity.this));
        bottomNavComponent.setSelectedItem(R.id.nav_profile);

        btnShowProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Payment History button
        LinearLayout btnPaymentHistory = findViewById(R.id.btnPaymentHistory);
        if (btnPaymentHistory != null) {
            btnPaymentHistory.setOnClickListener(v -> {
                startActivity(new Intent(this, PaymentHistoryActivity.class));
            });
        }

        // Upgrade button
        if (btnUpgrade != null) {
            btnUpgrade.setOnClickListener(v -> {
                if (currentSubscription != null && currentSubscription.subscriptionId != null) {
                    Intent intent = new Intent(this, SubscriptionUpgradeActivity.class);
                    intent.putExtra("currentSubscriptionId", currentSubscription.subscriptionId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Không thể nâng cấp. Vui lòng đăng ký gói trước.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Cancel button
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                showCancelSubscriptionDialog();
            });
        }

        // Subscription button click - Set listener trực tiếp
        setupSubscriptionClickListener();
        
        // Load user profile từ cache hoặc API
        loadUserProfile();
        
        // Load current subscription
        loadCurrentSubscription();
    }

    private void setupSubscriptionClickListener() {
        View.OnClickListener subscriptionClickListener = v -> {
            Log.d("SettingsActivity", "Subscription clicked! View: " + v.getClass().getSimpleName());
            Toast.makeText(SettingsActivity.this, "Opening Subscription...", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(SettingsActivity.this, SubscriptionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.d("SettingsActivity", "Started SubscriptionActivity");
            } catch (Exception e) {
                Log.e("SettingsActivity", "Error starting SubscriptionActivity", e);
                Toast.makeText(SettingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        };

        // Thử tìm view ngay
        LinearLayout btnSubscription = findViewById(R.id.btnSubscription);

        if (btnSubscription != null) {
            // Đảm bảo view có thể nhận click
            btnSubscription.setEnabled(true);
            btnSubscription.setClickable(true);
            btnSubscription.setFocusable(true);
            btnSubscription.setFocusableInTouchMode(true);

            // Set click listener
            btnSubscription.setOnClickListener(subscriptionClickListener);
            Log.d("SettingsActivity", "Set click listener for btnSubscription (direct)");
        } else {
            Log.w("SettingsActivity", "btnSubscription is NULL, will retry in post");
            // Retry trong post nếu view chưa sẵn sàng
            findViewById(android.R.id.content).post(() -> {
                LinearLayout retryBtn = findViewById(R.id.btnSubscription);
                if (retryBtn != null) {
                    retryBtn.setEnabled(true);
                    retryBtn.setClickable(true);
                    retryBtn.setFocusable(true);
                    retryBtn.setFocusableInTouchMode(true);
                    retryBtn.setOnClickListener(subscriptionClickListener);
                    Log.d("SettingsActivity", "Set click listener for btnSubscription (post)");
                } else {
                    Log.e("SettingsActivity", "btnSubscription is still NULL after post!");
                }
            });
        }

        // Disable CardView click
        com.google.android.material.card.MaterialCardView cardSubscription = findViewById(R.id.cardSubscription);
        if (cardSubscription != null) {
            cardSubscription.setClickable(false);
            cardSubscription.setFocusable(false);
            Log.d("SettingsActivity", "Disabled click on cardSubscription");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load user profile từ cache hoặc API
        loadUserProfile();
        // Reload current subscription khi quay lại từ SubscriptionActivity hoặc PaymentSuccessActivity
        // Luôn refresh từ API để đảm bảo dữ liệu mới nhất
        loadCurrentSubscription();
    }

    /**
     * Load user profile từ cache hoặc API
     * GET /api/User/roles/me
     */
    private void loadUserProfile() {
        // Thử load từ cache trước (nhanh hơn)
        UserRolesMeResponse cachedProfile = userManager.getUserProfile();
        if (cachedProfile != null) {
            Log.d("SettingsActivity", "Using cached user profile: " + cachedProfile.name);
            displayUserProfile(cachedProfile);
        } else {
            // Nếu không có cache, hiển thị empty state và refresh từ API
            displayEmptyUserProfile();
        }
        
        // Luôn refresh từ API để đảm bảo dữ liệu mới nhất
        userManager.refreshUserProfile(userProfile -> {
            if (userProfile != null) {
                Log.d("SettingsActivity", "User profile refreshed from API: " + userProfile.name);
                displayUserProfile(userProfile);
            } else {
                Log.d("SettingsActivity", "No user profile found after refresh");
                displayEmptyUserProfile();
            }
        });
    }

    /**
     * Hiển thị thông tin user profile
     */
    private void displayUserProfile(UserRolesMeResponse userProfile) {
        // Hiển thị name
        if (userProfile.name != null && !userProfile.name.isEmpty()) {
            tvUserName.setText(userProfile.name);
        } else {
            tvUserName.setText("Người dùng");
        }
        
        // Hiển thị email
        if (userProfile.email != null && !userProfile.email.isEmpty()) {
            tvUserEmail.setText(userProfile.email);
        } else {
            tvUserEmail.setText("");
        }
        
        // Hiển thị avatar từ Base64
        if (userProfile.avatarUrl != null && !userProfile.avatarUrl.isEmpty()) {
            loadAvatarFromBase64(userProfile.avatarUrl);
        } else {
            // Hiển thị placeholder nếu không có avatar
            imgAvatar.setImageResource(R.drawable.logo_learnmate);
        }
    }

    /**
     * Load avatar từ Base64 string
     */
    private void loadAvatarFromBase64(String base64String) {
        try {
            // Xử lý Base64 string - có thể có prefix "data:image/..." hoặc không
            String base64Image = base64String;
            if (base64String.contains(",")) {
                // Nếu có prefix, lấy phần sau dấu phẩy
                base64Image = base64String.substring(base64String.indexOf(",") + 1);
            }
            
            // Decode Base64 string thành byte array
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            
            // Tạo Bitmap từ byte array
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            
            if (bitmap != null) {
                imgAvatar.setImageBitmap(bitmap);
            } else {
                Log.w("SettingsActivity", "Failed to decode Base64 image");
                imgAvatar.setImageResource(R.drawable.logo_learnmate);
            }
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error loading avatar from Base64", e);
            imgAvatar.setImageResource(R.drawable.logo_learnmate);
        }
    }

    /**
     * Hiển thị empty state khi không có user profile
     */
    private void displayEmptyUserProfile() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = sp.getString("user_name", "");
        String email = sp.getString("user_email", "");
        tvUserName.setText(name.isEmpty() ? "Người dùng" : name);
        tvUserEmail.setText(email);
        imgAvatar.setImageResource(R.drawable.logo_learnmate);
    }
    
    /**
     * Load current subscription từ cache hoặc API
     * GET /api/Subscription/plans/my/current
     */
    private void loadCurrentSubscription() {
        // Thử load từ cache trước (nhanh hơn)
        com.example.LearnMate.managers.SubscriptionManager subscriptionManager = 
            com.example.LearnMate.managers.SubscriptionManager.getInstance(this);
        
        CurrentSubscriptionResponse cachedSubscription = subscriptionManager.getCurrentSubscription();
        if (cachedSubscription != null) {
            Log.d("SettingsActivity", "Using cached subscription: " + cachedSubscription.name);
            displayCurrentSubscription(cachedSubscription);
        } else {
            // Nếu không có cache, hiển thị empty state và refresh từ API
            displayEmptySubscription();
        }
        
        // Luôn refresh từ API để đảm bảo dữ liệu mới nhất
        subscriptionManager.refreshSubscription(subscription -> {
            if (subscription != null) {
                Log.d("SettingsActivity", "Subscription refreshed from API: " + subscription.name);
                displayCurrentSubscription(subscription);
            } else {
                Log.d("SettingsActivity", "No subscription found after refresh");
                displayEmptySubscription();
            }
        });
    }
    
    /**
     * Hiển thị thông tin current subscription
     */
    private void displayCurrentSubscription(CurrentSubscriptionResponse subscription) {
        currentSubscription = subscription;
        
        // Hiển thị tên gói
        tvCurrentPlanName.setText(subscription.name != null ? subscription.name : "Gói đăng ký");
        
        // Kiểm tra status - API trả về "Current" hoặc subscriptionStatus là "active"
        boolean isActive = (subscription.status != null && 
                           (subscription.status.equalsIgnoreCase("ACTIVE") || 
                            subscription.status.equalsIgnoreCase("Current"))) ||
                          (subscription.subscriptionStatus != null && 
                           subscription.subscriptionStatus.equalsIgnoreCase("active"));
        
        if (isActive) {
            tvCurrentPlanBadge.setVisibility(View.VISIBLE);
            tvCurrentPlanStatus.setVisibility(View.VISIBLE);
            tvCurrentPlanStatus.setText("Hoạt Động");
            // Hiển thị nút Upgrade và Cancel nếu có subscription
            if (btnUpgrade != null) {
                btnUpgrade.setVisibility(View.VISIBLE);
            }
            if (btnCancel != null) {
                btnCancel.setVisibility(View.VISIBLE);
            }
        } else {
            tvCurrentPlanBadge.setVisibility(View.GONE);
            tvCurrentPlanStatus.setVisibility(View.GONE);
            if (btnUpgrade != null) {
                btnUpgrade.setVisibility(View.GONE);
            }
            if (btnCancel != null) {
                btnCancel.setVisibility(View.GONE);
            }
        }
        
        // Build thông tin chi tiết từ subscription
        StringBuilder details = new StringBuilder();
        
        // Build features string từ plan type
        String features = buildFeaturesFromType(subscription.type);
        if (features != null && !features.isEmpty()) {
            details.append(features);
        }
        
        // Thêm thông tin giá nếu có
        if (subscription.originalPrice > 0) {
            if (details.length() > 0) {
                details.append("\n");
            }
            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
            String priceText = formatter.format(subscription.getFinalPrice()) + " VND";
            if (subscription.discount > 0) {
                String originalPriceText = formatter.format(subscription.originalPrice) + " VND";
                details.append("\n• Giá gốc: ").append(originalPriceText);
                details.append("\n• Giảm giá: -").append(subscription.discount).append("%");
                details.append("\n• Giá cuối: ").append(priceText);
            } else {
                details.append("\n• Giá: ").append(priceText);
            }
        }
        
        // Thêm thông tin ngày đăng ký và hết hạn
        if (subscription.subscribedAt != null && !subscription.subscribedAt.isEmpty()) {
            if (details.length() > 0) {
                details.append("\n");
            }
            String subscribedDate = formatDate(subscription.subscribedAt);
            details.append("\n• Đăng ký: ").append(subscribedDate);
        }
        
        if (subscription.expiredAt != null && !subscription.expiredAt.isEmpty()) {
            if (details.length() > 0) {
                details.append("\n");
            }
            String expiredDate = formatDate(subscription.expiredAt);
            details.append("\n• Hết hạn: ").append(expiredDate);
        }
        
        // Thêm loại gói
        if (subscription.type != null && !subscription.type.isEmpty()) {
            if (details.length() > 0) {
                details.append("\n");
            }
            String typeText = translateType(subscription.type);
            details.append("\n• Loại: ").append(typeText);
        }
        
        if (details.length() > 0) {
            tvCurrentPlanFeatures.setText(details.toString());
            tvCurrentPlanFeatures.setVisibility(View.VISIBLE);
        } else {
            tvCurrentPlanFeatures.setVisibility(View.GONE);
        }
    }
    
    /**
     * Format date từ ISO string sang định dạng dễ đọc
     */
    private String formatDate(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "";
        }
        
        try {
            // Parse ISO 8601 format: "2025-11-07T15:45:09.380757Z" (có thể có milliseconds và 'Z')
            boolean hasZ = isoDateString.endsWith("Z");
            String dateStr = hasZ ? isoDateString.substring(0, isoDateString.length() - 1) : isoDateString;
            
            java.text.SimpleDateFormat inputFormat;
            java.util.Date date = null;
            
            // Thử parse với format có milliseconds (có thể là 3, 6, hoặc nhiều hơn chữ số)
            if (dateStr.contains(".")) {
                int dotIndex = dateStr.indexOf(".");
                String baseDate = dateStr.substring(0, dotIndex);
                String milliseconds = dateStr.substring(dotIndex + 1);
                
                // Chỉ lấy 3 chữ số đầu của milliseconds (SimpleDateFormat chỉ hỗ trợ tối đa 3 chữ số)
                if (milliseconds.length() > 3) {
                    milliseconds = milliseconds.substring(0, 3);
                }
                
                String dateWithMs = baseDate + "." + milliseconds;
                inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US);
                if (hasZ) {
                    inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }
                
                try {
                    date = inputFormat.parse(dateWithMs);
                } catch (java.text.ParseException e) {
                    // Fallback: thử parse không có milliseconds
                    inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
                    if (hasZ) {
                        inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    }
                    date = inputFormat.parse(baseDate);
                }
            } else {
                // Format: "2025-11-07T15:45:09" (không có milliseconds)
                inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
                if (hasZ) {
                    inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }
                date = inputFormat.parse(dateStr);
            }
            
            if (date != null) {
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (java.text.ParseException e) {
            // Nếu không parse được, trả về nguyên bản hoặc format đơn giản
            Log.w("SettingsActivity", "Error parsing date: " + isoDateString, e);
            // Trả về phần date nếu có (trước chữ T)
            if (isoDateString.contains("T")) {
                return isoDateString.substring(0, 10); // Lấy "yyyy-MM-dd"
            }
            return isoDateString.length() > 10 ? isoDateString.substring(0, 10) : isoDateString;
        }
        
        return isoDateString;
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
     * Hiển thị khi không có subscription
     */
    private void displayEmptySubscription() {
        currentSubscription = null;
        tvCurrentPlanName.setText("Chưa có gói đăng ký");
        tvCurrentPlanBadge.setVisibility(View.GONE);
        tvCurrentPlanStatus.setVisibility(View.GONE);
        tvCurrentPlanFeatures.setText("• Không có đăng ký hoạt động\n• Chọn gói bên dưới để bắt đầu");
        tvCurrentPlanFeatures.setVisibility(View.VISIBLE);
        if (btnUpgrade != null) {
            btnUpgrade.setVisibility(View.GONE);
        }
        if (btnCancel != null) {
            btnCancel.setVisibility(View.GONE);
        }
    }
    
    /**
     * Hiển thị dialog xác nhận hủy subscription
     */
    private void showCancelSubscriptionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy gói")
                .setMessage("Bạn có chắc chắn muốn hủy gói hiện tại?")
                .setPositiveButton("Có", (dialog, which) -> {
                    cancelSubscription();
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    /**
     * Gọi API để hủy subscription
     * POST /api/Subscription/plans/cancel
     */
    private void cancelSubscription() {
        if (subscriptionService == null) {
            Toast.makeText(this, "Lỗi: Không thể kết nối đến dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        Toast.makeText(this, "Đang hủy gói...", Toast.LENGTH_SHORT).show();
        
        subscriptionService.cancelSubscription().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Hủy thành công
                    Toast.makeText(SettingsActivity.this, "Đã hủy gói thành công", Toast.LENGTH_SHORT).show();
                    
                    // Refresh subscription cache
                    SubscriptionManager.getInstance(SettingsActivity.this).loadSubscriptionFromAPI();
                    
                    // Refresh UI
                    loadCurrentSubscription();
                } else {
                    // Hủy thất bại
                    String errorMsg = "Không thể hủy gói. Vui lòng thử lại.";
                    if (response.code() == 404) {
                        errorMsg = "Không tìm thấy gói đăng ký để hủy";
                    } else if (response.code() == 400) {
                        errorMsg = "Không thể hủy gói. Vui lòng kiểm tra lại.";
                    }
                    Toast.makeText(SettingsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("SettingsActivity", "Cancel subscription failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMsg = "Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "Không xác định");
                Toast.makeText(SettingsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e("SettingsActivity", "Error canceling subscription", t);
            }
        });
    }
    
    /**
     * Build features string từ plan type
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
}
