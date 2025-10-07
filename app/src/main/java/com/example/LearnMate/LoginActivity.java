// com.example.lab6.LoginActivity
package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
    private MaterialButton  btnGoogle;

    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in); // khớp với XML bạn gửi

        // ---- match đúng ID trong activity_login.xml ----
        btnBack = findViewById(R.id.btnBack);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogle);
        forgotPassword = findViewById(R.id.forgotPassword);
        goToSignUp = findViewById(R.id.goToSignUp);

        presenter = new LoginPresenter(this);

        // ---- Events ----
        btnBack.setOnClickListener(v -> finish());

        btnSignIn.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) { showErrorMessage("Vui lòng nhập email"); return; }
            if (TextUtils.isEmpty(password)) { showErrorMessage("Vui lòng nhập mật khẩu"); return; }

            presenter.performLogin(email, password);
        });

        goToSignUp.setOnClickListener(v -> presenter.onSignupClicked());

        forgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "TODO: Forgot password flow", Toast.LENGTH_SHORT).show()
        );

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "TODO: Google login", Toast.LENGTH_SHORT).show()
        );
    }

    // ---- LoginView implementation ----
    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToSignup() {
        startActivity(new Intent(this, SignupActivity.class));
        finish();
    }

    @Override
    public void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class)); // <— chuyển qua HomeActivity
        finish(); // đóng màn login để không quay lại bằng back
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
