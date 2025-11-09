package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.ForgotPasswordRequest;
import com.example.LearnMate.network.dto.ForgotPasswordResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private MaterialButton btnSendEmail;
    private ImageButton btnBack;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        inputEmail = findViewById(R.id.inputEmail);
        btnSendEmail = findViewById(R.id.btnSendEmail);

        // Initialize AuthService (forgot password doesn't need auth token)
        authService = RetrofitClient.get().create(AuthService.class);

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());

        btnSendEmail.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            sendForgotPasswordEmail(email);
        });
    }

    private void sendForgotPasswordEmail(String email) {
        btnSendEmail.setEnabled(false);
        btnSendEmail.setText("Đang gửi...");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        authService.forgotPassword(request).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                btnSendEmail.setEnabled(true);
                btnSendEmail.setText("GỬI EMAIL");

                if (response.isSuccessful() && response.body() != null) {
                    ForgotPasswordResponse forgotResponse = response.body();
                    // Navigate to verify token screen with userId and token
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyTokenActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("userId", forgotResponse.userId);
                    intent.putExtra("token", forgotResponse.token);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Gửi email thất bại";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = "Lỗi: " + response.code();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                btnSendEmail.setEnabled(true);
                btnSendEmail.setText("GỬI EMAIL");
                Toast.makeText(ForgotPasswordActivity.this, 
                    "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Vui lòng thử lại"), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}

