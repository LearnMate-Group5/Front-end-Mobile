package com.example.learnmate_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learnmate_frontend.R;
import com.example.learnmate_frontend.util.SharedPrefManager;

public class WelcomeActivity extends AppCompatActivity {
    private Button btnSignUp;
    private TextView goToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra nếu đã đăng nhập thì chuyển thẳng đến Home
        if (SharedPrefManager.getInstance(this).getToken() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_welcome);
        
        // Ánh xạ các view
        btnSignUp = findViewById(R.id.btnSignUp);
        goToSignIn = findViewById(R.id.goToSignIn);
        
        // Sự kiện Sign Up
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
        
        // Sự kiện Sign In
        goToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }
}
