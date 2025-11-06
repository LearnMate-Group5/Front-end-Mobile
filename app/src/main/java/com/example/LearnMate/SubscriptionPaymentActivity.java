package com.example.LearnMate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubscriptionPaymentActivity extends AppCompatActivity {
    
    private static final String EXTRA_PLAN_NAME = "plan_name";
    private static final String EXTRA_PLAN_PRICE = "plan_price";
    private static final String EXTRA_PLAN_ORIGINAL_PRICE = "plan_original_price";
    private static final String EXTRA_PLAN_DISCOUNT = "plan_discount";
    private static final String EXTRA_PLAN_TYPE = "plan_type";
    private static final String EXTRA_PLAN_FEATURES = "plan_features";
    private static final String EXTRA_PLAN_SUBSCRIPTION_ID = "plan_subscription_id";
    private static final String EXTRA_CURRENT_PLAN_NAME = "current_plan_name";
    private static final String EXTRA_CURRENT_PLAN_TYPE = "current_plan_type";
    
    private TextView tvPlanTitle;
    private TextView tvPlanSubtitle;
    private TextView tvOriginalPrice;
    private TextView tvDiscount;
    private TextView tvFinalPrice;
    private TextView tvCurrentPlanLabel;
    private TextView tvSelectedPlanLabel;
    private TextView tvPriceInfo;
    private LinearLayout llFeaturesList;
    private MaterialButton btnPay;
    
    private SubscriptionActivity.SubscriptionPlan selectedPlan;
    private SubscriptionActivity.SubscriptionPlan currentPlan;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_payment);
        
        // Get data from intent
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        
        // Get selected plan data
        String planName = intent.getStringExtra(EXTRA_PLAN_NAME);
        long planPrice = intent.getLongExtra(EXTRA_PLAN_PRICE, 0);
        long planOriginalPrice = intent.getLongExtra(EXTRA_PLAN_ORIGINAL_PRICE, 0);
        int planDiscount = intent.getIntExtra(EXTRA_PLAN_DISCOUNT, 0);
        String planType = intent.getStringExtra(EXTRA_PLAN_TYPE);
        String planFeatures = intent.getStringExtra(EXTRA_PLAN_FEATURES);
        String planSubscriptionId = intent.getStringExtra(EXTRA_PLAN_SUBSCRIPTION_ID);
        
        // Get current plan data
        String currentPlanName = intent.getStringExtra(EXTRA_CURRENT_PLAN_NAME);
        String currentPlanType = intent.getStringExtra(EXTRA_CURRENT_PLAN_TYPE);
        
        // Create plan objects
        selectedPlan = new SubscriptionActivity.SubscriptionPlan(
            planName, planPrice, planOriginalPrice, planDiscount,
            "", planFeatures, planType, false, planSubscriptionId
        );
        
        if (currentPlanName != null) {
            currentPlan = new SubscriptionActivity.SubscriptionPlan(
                currentPlanName, 0, 0, 0,
                "", "", currentPlanType != null ? currentPlanType : "FREE",
                true, null
            );
        } else {
            currentPlan = new SubscriptionActivity.SubscriptionPlan(
                "Free", 0, 0, 0,
                "", "", "FREE", true, null
            );
        }
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Bind views
        tvPlanTitle = findViewById(R.id.tvPlanTitle);
        tvPlanSubtitle = findViewById(R.id.tvPlanSubtitle);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvFinalPrice = findViewById(R.id.tvFinalPrice);
        tvCurrentPlanLabel = findViewById(R.id.tvCurrentPlanLabel);
        tvSelectedPlanLabel = findViewById(R.id.tvSelectedPlanLabel);
        tvPriceInfo = findViewById(R.id.tvPriceInfo);
        llFeaturesList = findViewById(R.id.llFeaturesList);
        btnPay = findViewById(R.id.btnPay);
        
        // Setup Pay button
        btnPay.setOnClickListener(v -> {
            // TODO: Implement payment logic khi có API
            Log.d("SubscriptionPaymentActivity", "Pay button clicked for plan: " + selectedPlan.getName());
            Toast.makeText(this, "Payment feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadData() {
        // Set title
        tvPlanTitle.setText(selectedPlan.getName());
        
        // Set subtitle
        tvPlanSubtitle.setText("Quyền truy cập mở rộng hơn vào các tính năng phổ biến nhất của chúng tôi");
        
        // Set price
        tvFinalPrice.setText(selectedPlan.getFormattedPrice());
        
        // Set original price and discount
        if (selectedPlan.hasDiscount()) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvDiscount.setVisibility(View.VISIBLE);
            
            tvOriginalPrice.setText(selectedPlan.getFormattedOriginalPrice());
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            
            tvDiscount.setText(selectedPlan.getFormattedDiscount());
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscount.setVisibility(View.GONE);
        }
        
        // Set plan labels
        tvCurrentPlanLabel.setText(getCurrentPlanDisplayName());
        tvSelectedPlanLabel.setText(selectedPlan.getName());
        
        // Set price info
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        String priceText = "Gia hạn với giá " + formatter.format(selectedPlan.getPrice()) + " đ/tháng. Huỷ bất kỳ lúc nào.";
        tvPriceInfo.setText(priceText);
        
        // Build features comparison
        buildFeaturesComparison();
    }
    
    private String getCurrentPlanDisplayName() {
        if (currentPlan == null || currentPlan.getType().equals("FREE")) {
            return "Free";
        }
        return currentPlan.getName();
    }
    
    private void buildFeaturesComparison() {
        llFeaturesList.removeAllViews();
        
        // Parse features from selected plan
        List<String> selectedFeatures = parseFeatures(selectedPlan.getFeatures());
        List<String> currentFeatures = parseFeatures(getCurrentPlanFeatures());
        
        // Get all unique features
        List<String> allFeatures = new ArrayList<>();
        allFeatures.addAll(selectedFeatures);
        for (String feature : currentFeatures) {
            if (!allFeatures.contains(feature)) {
                allFeatures.add(feature);
            }
        }
        
        // Create feature comparison rows
        for (String feature : allFeatures) {
            View featureRow = LayoutInflater.from(this).inflate(R.layout.item_feature_comparison, llFeaturesList, false);
            
            TextView tvFeatureName = featureRow.findViewById(R.id.tvFeatureName);
            TextView tvCurrentPlanStatus = featureRow.findViewById(R.id.tvCurrentPlanStatus);
            TextView tvSelectedPlanStatus = featureRow.findViewById(R.id.tvSelectedPlanStatus);
            
            tvFeatureName.setText(feature);
            
            // Check if feature exists in current plan
            boolean hasInCurrent = currentFeatures.contains(feature);
            if (hasInCurrent) {
                tvCurrentPlanStatus.setText("✓");
                tvCurrentPlanStatus.setTextColor(getResources().getColor(R.color.purple_primary, null));
            } else {
                tvCurrentPlanStatus.setText("—");
                tvCurrentPlanStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
            
            // Check if feature exists in selected plan
            boolean hasInSelected = selectedFeatures.contains(feature);
            if (hasInSelected) {
                tvSelectedPlanStatus.setText("✓");
                tvSelectedPlanStatus.setTextColor(getResources().getColor(R.color.purple_primary, null));
            } else {
                tvSelectedPlanStatus.setText("—");
                tvSelectedPlanStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
            
            llFeaturesList.addView(featureRow);
        }
    }
    
    private List<String> parseFeatures(String featuresText) {
        List<String> features = new ArrayList<>();
        if (featuresText == null || featuresText.isEmpty()) {
            return features;
        }
        
        String[] lines = featuresText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("•")) {
                line = line.substring(1).trim();
            }
            if (!line.isEmpty()) {
                features.add(line);
            }
        }
        
        return features;
    }
    
    private String getCurrentPlanFeatures() {
        if (currentPlan == null || currentPlan.getType().equals("FREE")) {
            return "• 5 PDF imports per month\n• Basic AI features\n• Limited storage";
        }
        return currentPlan.getFeatures();
    }
    
    /**
     * Static method để start Activity này
     */
    public static void start(Context context, SubscriptionActivity.SubscriptionPlan selectedPlan, SubscriptionActivity.SubscriptionPlan currentPlan) {
        Intent intent = new Intent(context, SubscriptionPaymentActivity.class);
        intent.putExtra(EXTRA_PLAN_NAME, selectedPlan.getName());
        intent.putExtra(EXTRA_PLAN_PRICE, selectedPlan.getPrice());
        intent.putExtra(EXTRA_PLAN_ORIGINAL_PRICE, selectedPlan.getOriginalPrice());
        intent.putExtra(EXTRA_PLAN_DISCOUNT, selectedPlan.getDiscount());
        intent.putExtra(EXTRA_PLAN_TYPE, selectedPlan.getType());
        intent.putExtra(EXTRA_PLAN_FEATURES, selectedPlan.getFeatures());
        intent.putExtra(EXTRA_PLAN_SUBSCRIPTION_ID, selectedPlan.getSubscriptionId());
        
        if (currentPlan != null) {
            intent.putExtra(EXTRA_CURRENT_PLAN_NAME, currentPlan.getName());
            intent.putExtra(EXTRA_CURRENT_PLAN_TYPE, currentPlan.getType());
        }
        
        context.startActivity(intent);
    }
}

