package com.example.LearnMate.view;

public interface ProfileView {
    void showUsername(String username);
    void showEmail(String email);
    void setEditMode(boolean editMode);
    void setEditButtonText(String text);
    void setEditButtonIcon(int iconRes);
    void setFieldsEnabled(boolean enabled);
    void setEditButtonEnabled(boolean enabled);
    void showSuccessMessage(String message);
    void showErrorMessage(String message);
    void navigateToSettings();
    void performLogout();
}

