package com.example.LearnMate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.ApiResult;
import com.example.LearnMate.network.dto.UpdateUserProfileRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView btnFooterSettings;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etAvatarUrl; // nếu không dùng avatar, có thể bỏ và sửa body
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout;
    private BottomNavigationComponent bottomNavComponent;
    private SessionManager sessionManager;

    private boolean isEditMode = false;

    private AuthService authService;
    private String userIdFromSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(getApplicationContext());
        authService = RetrofitClient.getAuthService(getApplicationContext());

        // bind views
        btnBack = findViewById(R.id.btnBack);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etAvatarUrl = findViewById(R.id.etAvatarUrl); // đảm bảo layout có id này, hoặc bỏ nếu không dùng
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        setEditMode(false);
        preloadFromSession();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            if (isEditMode) saveProfileData();
            else setEditMode(true);
        });

        btnLogout.setOnClickListener(v -> sessionManager.logout(ProfileActivity.this));
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        etUsername.setEnabled(editMode);
        etEmail.setEnabled(editMode);
        if (etAvatarUrl != null) etAvatarUrl.setEnabled(editMode);

        if (editMode) {
            btnEditProfile.setText("Save");
            btnEditProfile.setIconResource(android.R.drawable.ic_menu_save);
        } else {
            btnEditProfile.setText("Edit Profile");
            btnEditProfile.setIconResource(android.R.drawable.ic_menu_edit);
        }
    }

    private void preloadFromSession() {
        SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userIdFromSession = sp.getString("user_id", null);
        String userName = sp.getString("user_name", "");
        String userEmail = sp.getString("user_email", "");
        String avatar = sp.getString("avatar_url", "");

        etUsername.setText(userName);
        etEmail.setText(userEmail);
        if (etAvatarUrl != null) etAvatarUrl.setText(avatar);
    }

    private void saveProfileData() {
        String name  = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String avatarUrl = etAvatarUrl != null ? etAvatarUrl.getText().toString().trim() : "";

        if (userIdFromSession == null || userIdFromSession.isEmpty()) {
            Toast.makeText(this, "Missing userId in session", Toast.LENGTH_SHORT).show();
            setEditMode(true);
            return;
        }
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name & Email are required", Toast.LENGTH_SHORT).show();
            setEditMode(true);
            return;
        }

        UpdateUserProfileRequest body = new UpdateUserProfileRequest(name, email, avatarUrl);

        setEditMode(false);
        btnEditProfile.setEnabled(false);

        authService.updateUserProfile(userIdFromSession, body)
                .enqueue(new Callback<ApiResult<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResult<Object>> call, Response<ApiResult<Object>> response) {
                        btnEditProfile.setEnabled(true);
                        if (response.isSuccessful() && response.body()!=null && response.body().isSuccess) {
                            Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();

                            SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            sp.edit()
                                    .putString("user_name", name)
                                    .putString("user_email", email)
                                    .putString("avatar_url", avatarUrl)
                                    .apply();

                            setEditMode(false);
                        } else {
                            setEditMode(true);
                            String msg = "Update failed";
                            if (response.body()!=null && response.body().error!=null
                                    && response.body().error.description!=null) {
                                msg = response.body().error.description;
                            }
                            Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResult<Object>> call, Throwable t) {
                        btnEditProfile.setEnabled(true);
                        setEditMode(true);
                        Toast.makeText(ProfileActivity.this, "Network error: " + (t.getMessage()==null?"":t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
