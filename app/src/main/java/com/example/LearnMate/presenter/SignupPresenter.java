package com.example.LearnMate.presenter;

import com.example.LearnMate.model.AuthModel;
import com.example.LearnMate.view.SignupView;

public class SignupPresenter {
    private SignupView view;
    private AuthModel model;

    public SignupPresenter(SignupView view) {
        this.view = view;
        this.model = new AuthModel();
    }

    public void performSignup(String email, String password, String confirmPassword, String username) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            view.showSignupError("All fields must be filled");
            return;
        }
        if (!password.equals(confirmPassword)) {
            view.showSignupError("Passwords do not match");
            return;
        }
        model.signup(email, password, username, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                view.showSignupSuccess(message);
                view.navigateToLogin();
            }
            @Override
            public void onFailure(String error) {
                view.showSignupError(error);
            }
        });
    }

    public void onLoginClicked() {
        view.navigateToLogin();
    }
}