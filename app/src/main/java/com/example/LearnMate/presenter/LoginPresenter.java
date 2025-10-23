package com.example.LearnMate.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.LearnMate.model.AuthModel;
import com.example.LearnMate.network.dto.AuthPayload;
import com.example.LearnMate.view.LoginView;

public class LoginPresenter {

    private final LoginView view;
    private final Context appContext;
    private final AuthModel model;

    public LoginPresenter(LoginView view, Context context) {
        this.view = view;
        this.appContext = context.getApplicationContext();
        this.model = new AuthModel(appContext);
    }

    public void performLogin(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            view.showErrorMessage("Please enter email and password");
            return;
        }

        model.login(email, password, new AuthModel.AuthCallback() {
            @Override public void onSuccess(AuthPayload payload) {
                // Lưu token + user info
                SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("token", payload.accessToken)
                        .putString("refresh_token", payload.refreshToken)
                        .putString("expires_at", payload.expiresAt)
                        .putString("user_id", payload.user != null ? payload.user.userId : null)
                        .putString("user_name", payload.user != null ? payload.user.name : null)
                        .putString("user_email", payload.user != null ? payload.user.email : null)
                        .putBoolean("is_logged_in", true)
                        .apply();

                view.showSuccessMessage("Login successful");
                view.navigateToHome();
            }

            @Override public void onFailure(String message) {
                view.showErrorMessage(message);
            }
        });
    }

    public void onSignupClicked() { view.navigateToSignup(); }
}
