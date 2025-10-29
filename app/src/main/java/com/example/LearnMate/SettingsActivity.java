package com.example.LearnMate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        // btnSettings = findViewById(R.id.btnSettings); // This ID does not exist in the layout
        bottomNavComponent = findViewById(R.id.bottomNavComponent);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> sessionManager.logout(SettingsActivity.this));
        bottomNavComponent.setSelectedItem(R.id.nav_profile);

        btnShowProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindUserFromSession();
    }

    private void bindUserFromSession() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name  = sp.getString("user_name", "");
        String email = sp.getString("user_email", "");
        tvUserName.setText(name);
        tvUserEmail.setText(email);
    }
}
