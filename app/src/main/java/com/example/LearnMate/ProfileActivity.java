package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.presenter.ProfilePresenter;
import com.example.LearnMate.view.ProfileView;

public class ProfileActivity extends AppCompatActivity implements ProfileView {

    private ImageButton btnBack;
    private EditText etUsername;
    private EditText etEmail;
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout;
    private BottomNavigationComponent bottomNavComponent;
    private SessionManager sessionManager;

    private ProfilePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Presenter
        presenter = new ProfilePresenter(this, this);
        sessionManager = new SessionManager(getApplicationContext());

        // bind views
        btnBack = findViewById(R.id.btnBack);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Load profile data
        presenter.loadProfile();

        // Setup click listeners
        btnBack.setOnClickListener(v -> presenter.navigateToSettings());

        btnEditProfile.setOnClickListener(v -> {
            if (presenter.isEditMode()) {
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                presenter.saveProfile(username, email);
            } else {
                presenter.toggleEditMode();
            }
        });

        btnLogout.setOnClickListener(v -> presenter.performLogout());
    }

    // ================== ProfileView Implementation ==================

    @Override
    public void showUsername(String username) {
        etUsername.setText(username);
    }

    @Override
    public void showEmail(String email) {
        etEmail.setText(email);
    }

    @Override
    public void setEditMode(boolean editMode) {
        // This is handled by setFieldsEnabled
    }

    @Override
    public void setEditButtonText(String text) {
        btnEditProfile.setText(text);
    }

    @Override
    public void setEditButtonIcon(int iconRes) {
        btnEditProfile.setIconResource(iconRes);
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {
        etUsername.setEnabled(enabled);
        etEmail.setEnabled(enabled);
    }

    @Override
    public void setEditButtonEnabled(boolean enabled) {
        btnEditProfile.setEnabled(enabled);
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToSettings() {
        Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void performLogout() {
        sessionManager.logout(ProfileActivity.this);
    }
}
