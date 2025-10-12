package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnShowProfile;
    private ImageView btnSettings;

    private BottomNavigationComponent bottomNavComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnShowProfile = findViewById(R.id.btnShowProfile);
        btnSettings = findViewById(R.id.btnSettings);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Set click listeners
        btnShowProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Profile screen
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Profile screen when clicking on username area
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle settings icon click
                // You can add settings functionality here
            }
        });

        // Setup bottom navigation
        bottomNavComponent.setSelectedItem(R.id.nav_profile);
        // Navigation is now handled automatically by BottomNavigationComponent
    }
}
