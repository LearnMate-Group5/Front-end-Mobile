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
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeText = findViewById(R.id.welcomeTitle);
        btnLogout = findViewById(R.id.btnSignUp);

        SharedPreferences sharedPreferences = getSharedPreferences("LearnMatePrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("USER_NAME", null);

        if (username != null) {
            displayUserInfo(username);
        } else {
            // No user is logged in, redirect to the Login screen.
            goToLogin();
            return; // Stop further execution.
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user session
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Redirect to LoginActivity
                goToLogin();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
