package com.example.LearnMate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.view.WelcomeView;
import com.example.LearnMate.R;

public class WelcomeActivity extends AppCompatActivity implements WelcomeView {
    private TextView welcomeText;
    private Button btnSignUp;
    private TextView goToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra token trước khi hiển thị layout
        checkAuthenticationStatus();
    }

    private void checkAuthenticationStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (token != null && !token.isEmpty() && isLoggedIn) {
            // Có token và đã đăng nhập -> chuyển đến HomeActivity
            goToHome();
            return;
        } else {
            // Không có token -> hiển thị WelcomeActivity
            showWelcomeScreen();
        }
    }

    private void showWelcomeScreen() {
        setContentView(R.layout.activity_welcome);

        welcomeText = findViewById(R.id.welcomeTitle);
        btnSignUp = findViewById(R.id.btnSignUp);
        goToSignIn = findViewById(R.id.goToSignIn);

        // Set click listeners
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignup();
            }
        });

        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToSignup() {
        Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    @Override
    public void displayUserInfo(String username) {
        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Chào mừng, " + username + "!");
        } else {
            welcomeText.setText("Chào mừng!");
        }
    }
}