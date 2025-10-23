package com.example.LearnMate.presenter;

import android.content.Context;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.ApiResult;
import com.example.LearnMate.network.dto.AuthPayload;
import com.example.LearnMate.network.dto.RegisterUserCommand;
import com.example.LearnMate.view.SignupView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupPresenter {

    private final SignupView view;
    private final AuthService authService;

    // ✅ NHẬN Context để truyền vào RetrofitClient.getAuthService(context)
    public SignupPresenter(SignupView view, Context context) {
        this.view = view;
        this.authService = RetrofitClient.getAuthService(context.getApplicationContext());
    }

    public void performSignup(String email, String password, String confirmPassword, String username) {
        if (username == null || username.isEmpty()
                || email == null || email.isEmpty()
                || password == null || password.isEmpty()) {
            view.showSignupError("Please fill all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            view.showSignupError("Passwords do not match");
            return;
        }

        // ✅ Dùng đúng DTO theo Swagger: RegisterUserCommand { name, email, password }
        RegisterUserCommand body = new RegisterUserCommand(username, email, password);

        // ✅ AuthService.register trả ApiResult<AuthPayload>
        Call<ApiResult<AuthPayload>> call = authService.register(body);
        call.enqueue(new Callback<ApiResult<AuthPayload>>() {
            @Override
            public void onResponse(Call<ApiResult<AuthPayload>> call, Response<ApiResult<AuthPayload>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    view.showSignupSuccess("Registration successful! Please login.");
                    view.navigateToLogin();
                } else {
                    String msg = "Registration failed. Please try again.";
                    if (response.body() != null && response.body().error != null
                            && response.body().error.description != null && !response.body().error.description.isEmpty()) {
                        msg = response.body().error.description;
                    }
                    view.showSignupError(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResult<AuthPayload>> call, Throwable t) {
                view.showSignupError("Network error: " + (t.getMessage() == null ? "" : t.getMessage()));
            }
        });
    }

    public void onLoginClicked() {
        view.navigateToLogin();
    }
}
