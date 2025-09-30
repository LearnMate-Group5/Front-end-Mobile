package com.example.learnmate_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learnmate_frontend.R;
import com.example.learnmate_frontend.model.GenericResponse;
import com.example.learnmate_frontend.model.RegisterRequest;
import com.example.learnmate_frontend.network.ApiService;
import com.example.learnmate_frontend.network.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, passInput, confirmPassInput;
    private Button signUpBtn, googleBtn;
    private TextView goToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Ánh xạ các view
        nameInput = findViewById(R.id.inputName);
        emailInput = findViewById(R.id.inputEmail);
        passInput = findViewById(R.id.inputPassword);
        confirmPassInput = findViewById(R.id.inputConfirmPassword);
        signUpBtn = findViewById(R.id.btnSignUp);
        googleBtn = findViewById(R.id.btnGoogle);
        goToSignIn = findViewById(R.id.goToSignIn);

        // Sự kiện đăng ký
        signUpBtn.setOnClickListener(v -> handleSignUp());

        // Sự kiện đăng nhập
        goToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Google chưa hỗ trợ
        googleBtn.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign-Up chưa hỗ trợ", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleSignUp() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passInput.getText().toString();
        String confirmPassword = confirmPassInput.getText().toString();

        // Kiểm tra hợp lệ đầu vào
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Vui lòng điền đầy đủ thông tin");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Mật khẩu không khớp");
            return;
        }

        if (password.length() < 6) {
            showToast("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(email, password, name);

        ApiService apiService = RetrofitClient.getInstance(SignUpActivity.this);
        apiService.register(registerRequest).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showToast("Đăng ký thành công");
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                    finish();
                } else {
                    showToast("Đăng ký thất bại: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("SIGNUP_ERROR", response.errorBody().string());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                showToast("Lỗi kết nối đến máy chủ: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
