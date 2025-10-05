// com.example.su2025firebasefptlogin.view.LoginView
package com.example.LearnMate.view;

public interface LoginView {
    void showSuccessMessage(String message);
    void showLoginError(String error);
    void navigateToSignup();
    void navigateToHome();
    void showErrorMessage(String message);
}