package com.example.LearnMate.presenter;

import com.example.LearnMate.model.AuthModel;
import com.example.LearnMate.view.LoginView;

public class LoginPresenter {

    private LoginView view;

    private AuthModel Model;

    public LoginPresenter(LoginView view) {
        this.view = view;
        this.Model = new AuthModel();
    }

    public void performLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showErrorMessage("Email and password cannot be empty");
            return;
        }
        Model.login(email, password, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                view.showSuccessMessage(message);
                view.navigateToHome();
            }
            @Override
            public void onFailure(String error) {
                view.showLoginError(error);
            }
        });
    }
    public void onSignupClicked() {view.navigateToSignup();}
}
