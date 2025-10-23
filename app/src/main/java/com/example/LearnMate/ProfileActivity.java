package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.managers.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView btnFooterSettings;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private EditText etBirth;
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout; // Button for logging out
    private BottomNavigationComponent bottomNavComponent;
    private SessionManager sessionManager;

    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize SessionManager
        sessionManager = new SessionManager(getApplicationContext());

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBirth = findViewById(R.id.etBirth);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout); // Make sure you have a button with this ID in your XML layout
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Set initial state (view mode)
        setEditMode(false);

        // Set click listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Settings screen
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    // Save mode - save the data
                    saveProfileData();
                    setEditMode(false);
                } else {
                    // Edit mode - enable editing
                    setEditMode(true);
                }
            }
        });

        // Set logout button click listener
        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnLogout.setOnClickListener(y -> sessionManager.logout(ProfileActivity.this));
                }
            });
        }

        // Setup bottom navigation
        // ProfileActivity is not a main navigation item, so don't set selected item
        // Navigation is now handled automatically by BottomNavigationComponent
        // No need for manual navigation handling here
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;

        // Enable/disable EditText fields
        etUsername.setEnabled(editMode);
        etPassword.setEnabled(editMode);
        etEmail.setEnabled(editMode);
        etPhoneNumber.setEnabled(editMode);
        etBirth.setEnabled(editMode);

        // Change button text and icon
        if (editMode) {
            btnEditProfile.setText("Save");
            btnEditProfile.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_save, 0);
        } else {
            btnEditProfile.setText("Edit Profile");
            btnEditProfile.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_edit, 0);
        }
    }

    private void saveProfileData() {
        // Get data from EditText fields
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();
        String birth = etBirth.getText().toString();

        // TODO: Save data to database or SharedPreferences
        // For now, just show a toast or handle the save logic

        // You can add validation here
        if (username.isEmpty() || email.isEmpty()) {
            // Show error message
            return;
        }

        // Save successful - you can add your save logic here
    }
}
