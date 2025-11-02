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

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnShowProfile;
    private ImageView btnSettings;
    private LinearLayout btnLogout;

    private SessionManager sessionManager;
    private BottomNavigationComponent bottomNavComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnShowProfile = findViewById(R.id.btnShowProfile);
        // btnSettings = findViewById(R.id.btnSettings); // This ID does not exist in
        // the layout
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> sessionManager.logout(SettingsActivity.this));
        bottomNavComponent.setSelectedItem(R.id.nav_profile);

        btnShowProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Subscription button click - Set listener trực tiếp
        setupSubscriptionClickListener();
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
        bindUserFromSession();
    }

    private void bindUserFromSession() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = sp.getString("user_name", "");
        String email = sp.getString("user_email", "");
        tvUserName.setText(name);
        tvUserEmail.setText(email);
    }
}
