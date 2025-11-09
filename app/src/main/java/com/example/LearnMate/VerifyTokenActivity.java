package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.VerifyTokenResponse;
import com.example.LearnMate.network.dto.VerifyOtpResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyTokenActivity extends AppCompatActivity {

    private EditText inputToken, inputOtp;
    private MaterialButton btnVerifyToken, btnVerifyOtp;
    private ImageButton btnBack;
    private TextView textEmail, textTokenStatus;
    private AuthService authService;
    private String email;
    private String userId;
    private String token;
    private boolean tokenVerified = false; // Track if token has been verified

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_token);

        // Get data from intent
        email = getIntent().getStringExtra("email");
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");
        
        if (email == null || userId == null || token == null) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        textEmail = findViewById(R.id.textEmail);
        textTokenStatus = findViewById(R.id.textTokenStatus);
        inputToken = findViewById(R.id.inputToken);
        inputOtp = findViewById(R.id.inputOtp);
        btnVerifyToken = findViewById(R.id.btnVerifyToken);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        
        // Ensure token input is always hidden (never show token to user)
        inputToken.setVisibility(View.GONE);

        // Initialize AuthService
        authService = RetrofitClient.get().create(AuthService.class);

        // Set email text
        textEmail.setText("Email: " + email);
        
        // Initially disable OTP input and button until token is verified
        // Token will be verified automatically in background
        inputOtp.setEnabled(false);
        btnVerifyOtp.setEnabled(false);
        
        // Show loading state
        inputOtp.setHint("Đang xác thực token...");

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());

        // Token verification happens automatically in background
        // No need for manual button click

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = inputOtp.getText().toString().trim();
            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (otp.length() != 6) {
                Toast.makeText(this, "Mã OTP phải có 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }
            
            verifyOtp(otp);
        });
        
        // Auto-verify token on activity start
        verifyToken();
    }

    private void verifyToken() {
        // Token verification happens automatically in background
        // Show loading state in OTP input
        inputOtp.setHint("Đang xác thực token...");

        authService.verifyToken(userId, token).enqueue(new Callback<VerifyTokenResponse>() {
            @Override
            public void onResponse(Call<VerifyTokenResponse> call, Response<VerifyTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VerifyTokenResponse verifyResponse = response.body();
                    tokenVerified = true;
                    
                    // Token verified successfully in background
                    // Update UI to enable OTP input
                    inputOtp.setEnabled(true);
                    btnVerifyOtp.setEnabled(true);
                    inputOtp.setHint("Nhập mã OTP 6 chữ số");
                    inputOtp.requestFocus();
                    
                    // Show message that OTP has been sent to email
                    Toast.makeText(VerifyTokenActivity.this, 
                        "Mã OTP đã được gửi về email của bạn. Vui lòng kiểm tra email.", 
                        Toast.LENGTH_LONG).show();
                } else {
                    // Token verification failed
                    inputOtp.setHint("Xác thực token thất bại");
                    String errorMsg = "Xác thực token thất bại";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = "Lỗi: " + response.code();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Toast.makeText(VerifyTokenActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    // Allow user to go back
                }
            }

            @Override
            public void onFailure(Call<VerifyTokenResponse> call, Throwable t) {
                // Token verification failed due to network error
                inputOtp.setHint("Lỗi kết nối. Vui lòng thử lại.");
                Toast.makeText(VerifyTokenActivity.this, 
                    "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Vui lòng thử lại"), 
                    Toast.LENGTH_LONG).show();
                // Allow user to go back
            }
        });
    }

    private void verifyOtp(String otp) {
        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Đang xác thực OTP...");

        authService.verifyOtp(userId, otp).enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VerifyOtpResponse verifyResponse = response.body();
                    
                    // OTP verified successfully, navigate to reset password
                    Toast.makeText(VerifyTokenActivity.this, 
                        "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
                    
                    navigateToResetPassword(otp);
                } else {
                    btnVerifyOtp.setEnabled(true);
                    btnVerifyOtp.setText("XÁC THỰC OTP");
                    String errorMsg = "Xác thực OTP thất bại. Vui lòng kiểm tra lại mã OTP.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = "Lỗi: " + response.code();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Toast.makeText(VerifyTokenActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("XÁC THỰC OTP");
                Toast.makeText(VerifyTokenActivity.this, 
                    "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Vui lòng thử lại"), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToResetPassword(String otp) {
        Intent intent = new Intent(VerifyTokenActivity.this, ResetPasswordActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("userId", userId);
        intent.putExtra("otp", otp);
        startActivity(intent);
        finish();
    }
}

