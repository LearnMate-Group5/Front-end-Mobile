package com.example.LearnMate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.model.SubscriptionModel;
import com.example.LearnMate.model.SubscriptionPlan;
import com.example.LearnMate.payment.PayOSConstants;
import com.example.LearnMate.payment.PayOSPaymentHelper;
import com.example.LearnMate.presenter.SubscriptionPresenter;
import com.example.LearnMate.view.SubscriptionView;

import java.util.List;

public class SubscriptionActivity extends AppCompatActivity implements SubscriptionView {
    
    private RecyclerView rvSubscriptionPlans;
    private BottomNavigationComponent bottomNavComponent;
    private PayOSPaymentHelper payOSPaymentHelper;
    private ProgressDialog progressDialog;
    
    private SubscriptionPresenter presenter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.d("SubscriptionActivity", "onCreate called");
            setContentView(R.layout.activity_subscription);
            Log.d("SubscriptionActivity", "Layout set");
            
            // Initialize Presenter
            SubscriptionModel model = new SubscriptionModel(this);
            presenter = new SubscriptionPresenter(this, model);
            
            // Initialize Payment Helper (kept for compatibility)
            payOSPaymentHelper = new PayOSPaymentHelper(this);
            
            setupUI();
            presenter.loadSubscriptionData();
            
            Log.d("SubscriptionActivity", "Setup completed");
        } catch (Exception e) {
            Log.e("SubscriptionActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoadingDialog();
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
    
    // ================== SubscriptionView Implementation ==================
    
    @Override
    public void showLoading(String message) {
        dismissLoadingDialog();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    
    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }
    
    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    @Override
    public void showPlans(List<SubscriptionPlan> plans) {
        try {
            if (rvSubscriptionPlans != null) {
                SubscriptionAdapter adapter = new SubscriptionAdapter(
                    plans, 
                    presenter::onPlanSelected, 
                    presenter::onCancelClicked
                );
                rvSubscriptionPlans.setAdapter(adapter);
                Log.d("SubscriptionActivity", "Adapter updated with " + plans.size() + " plans");
            } else {
                Log.e("SubscriptionActivity", "rvSubscriptionPlans is NULL, cannot set adapter!");
            }
        } catch (Exception e) {
            Log.e("SubscriptionActivity", "Error setting adapter", e);
            e.printStackTrace();
        }
    }
    
    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void navigateToPayment(SubscriptionPlan selectedPlan, SubscriptionPlan currentPlan) {
        SubscriptionPaymentActivity.start(this, selectedPlan, currentPlan);
    }
    
    @Override
    public void showCancelConfirmationDialog(Runnable onConfirm) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Subscription")
            .setMessage("Are you sure you want to cancel your current subscription?")
            .setPositiveButton("Yes, Cancel", (dialog, which) -> onConfirm.run())
            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
            .show();
    }
    
    @Override
    public void finish() {
        super.finish();
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
}
