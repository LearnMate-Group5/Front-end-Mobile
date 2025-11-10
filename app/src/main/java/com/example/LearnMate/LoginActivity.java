package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.presenter.LoginPresenter;
import com.example.LearnMate.view.LoginView;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity implements LoginView {

    private EditText inputEmail, inputPassword;
    private MaterialButton btnSignIn;
    private TextView goToSignUp, forgotPassword;
    private ImageButton btnBack;
    
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        forgotPassword = findViewById(R.id.forgotPassword);
        goToSignUp = findViewById(R.id.goToSignUp);

        // Initialize Presenter with Context
        presenter = new LoginPresenter(this, this);

        // Set Click Listeners
        btnBack.setOnClickListener(v -> finish());

        btnSignIn.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            presenter.performLogin(email, password);
        });

        goToSignUp.setOnClickListener(v -> {
            navigateToSignup();
        });

        // Forgot Password - Navigate to ForgotPasswordActivity
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    // ---- LoginView Implementation ----

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Call the new navigation method
        navigateToHome();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToHome() {
        // Navigate to the main screen of the app
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity(); // Finish this activity and all parent activities
    }

    @Override
    public void navigateToSignup() {
        startActivity(new Intent(this, SignupActivity.class));
        finish();
    }
}
