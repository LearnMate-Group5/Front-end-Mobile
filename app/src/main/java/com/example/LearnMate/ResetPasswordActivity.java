package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.ResetPasswordCommand;
import com.example.LearnMate.network.dto.ResetPasswordResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText inputNewPassword, inputConfirmPassword;
    private MaterialButton btnResetPassword;
    private ImageButton btnBack;
    private TextView textEmail;
    private AuthService authService;
    private String email;
    private String token;
    private String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get data from intent
        email = getIntent().getStringExtra("email");
        String userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");
        otp = getIntent().getStringExtra("otp");

        if (email == null || otp == null) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        textEmail = findViewById(R.id.textEmail);
        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // Initialize AuthService
        authService = RetrofitClient.get().create(AuthService.class);

        // Set email text
        textEmail.setText("Email: " + email);

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = inputNewPassword.getText().toString().trim();
            String confirmPassword = inputConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPassword(newPassword, confirmPassword);
        });
    }

    private void resetPassword(String newPassword, String confirmPassword) {
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang đặt lại mật khẩu...");

        ResetPasswordCommand command = new ResetPasswordCommand(
            email,
            "", // Token không cần thiết khi đã verify OTP
            otp, // OTP là bắt buộc
            newPassword,
            confirmPassword
        );

        authService.resetPassword(command).enqueue(new Callback<ResetPasswordResponse>() {
            @Override
            public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> response) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("ĐẶT LẠI MẬT KHẨU");

                if (response.isSuccessful() && response.body() != null) {
                    // Password reset successful
                    String message = response.body().message != null ? 
                        response.body().message : "Đặt lại mật khẩu thành công!";
                    
                    Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate back to login
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Đặt lại mật khẩu thất bại";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = "Lỗi: " + response.code();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    Toast.makeText(ResetPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("ĐẶT LẠI MẬT KHẨU");
                Toast.makeText(ResetPasswordActivity.this, 
                    "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Vui lòng thử lại"), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}

