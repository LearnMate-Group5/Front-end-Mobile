package com.example.LearnMate.view;

public interface LoginView {
    void showSuccessMessage(String message);
    void showErrorMessage(String message);
    void navigateToSignup();
    void navigateToHome();
}
