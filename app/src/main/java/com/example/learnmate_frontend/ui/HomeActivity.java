package com.example.learnmate_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learnmate_frontend.R;
import com.example.learnmate_frontend.util.SharedPrefManager;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    
    private TextView welcomeText;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra đăng nhập
        if (SharedPrefManager.getInstance(this).getToken() == null) {
            Log.d(TAG, "No token found, redirecting to WelcomeActivity");
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_home);
        
        // Ánh xạ các view
        welcomeText = findViewById(R.id.welcomeText);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Hiển thị thông tin user
        displayUserInfo();
        
        // Sự kiện logout
        btnLogout.setOnClickListener(v -> logout());
    }
    
    private void displayUserInfo() {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        String userEmail = sharedPrefManager.getUser().getEmail();
        
        if (userEmail != null && !userEmail.isEmpty()) {
            welcomeText.setText("Chào mừng " + userEmail + "!");
        } else {
            welcomeText.setText("Chào mừng đến với LearnMate!");
        }
    }
    
    private void logout() {
        SharedPrefManager.getInstance(this).clear();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
