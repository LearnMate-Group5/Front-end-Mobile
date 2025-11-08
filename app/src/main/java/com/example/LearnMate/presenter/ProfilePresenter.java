package com.example.LearnMate.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.LearnMate.model.AuthModel;
import com.example.LearnMate.view.ProfileView;

public class ProfilePresenter {
    private static final String TAG = "ProfilePresenter";

    private final ProfileView view;
    private final Context appContext;
    private final AuthModel model;
    private boolean isEditMode = false;

    public ProfilePresenter(ProfileView view, Context context) {
        this.view = view;
        this.appContext = context.getApplicationContext();
        this.model = new AuthModel(appContext);
    }

    /**
     * Load user profile từ SharedPreferences
     */
    public void loadProfile() {
        SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userName = sp.getString("user_name", "");
        String userEmail = sp.getString("user_email", "");

        view.showUsername(userName);
        view.showEmail(userEmail);
        setEditMode(false);
    }

    /**
     * Get user ID từ SharedPreferences
     */
    public String getUserId() {
        SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return sp.getString("user_id", null);
    }

    /**
     * Toggle edit mode
     */
    public void toggleEditMode() {
        if (isEditMode) {
            // Nếu đang ở edit mode, không làm gì (save sẽ được gọi riêng)
            return;
        } else {
            setEditMode(true);
        }
    }

    /**
     * Set edit mode
     */
    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        view.setEditMode(editMode);
        view.setFieldsEnabled(editMode);

        if (editMode) {
            view.setEditButtonText("Lưu");
            view.setEditButtonIcon(android.R.drawable.ic_menu_save);
        } else {
            view.setEditButtonText("Chỉnh Sửa Hồ Sơ");
            view.setEditButtonIcon(android.R.drawable.ic_menu_edit);
        }
    }

    /**
     * Save profile data
     */
    public void saveProfile(String username, String email) {
        String userId = getUserId();

        // Validation
        if (userId == null || userId.isEmpty()) {
            view.showErrorMessage("Thiếu userId trong phiên đăng nhập");
            setEditMode(true);
            return;
        }

        if (username == null || username.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            view.showErrorMessage("Tên và Email là bắt buộc");
            setEditMode(true);
            return;
        }

        // Disable edit button while saving
        view.setEditButtonEnabled(false);
        setEditMode(false);

        // Call model to update profile
        model.updateProfile(userId, username.trim(), email.trim(), "", new AuthModel.ProfileCallback() {
            @Override
            public void onSuccess(String name, String email, String avatarUrl) {
                // Save to SharedPreferences
                SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("user_name", name)
                        .putString("user_email", email)
                        .putString("avatar_url", avatarUrl)
                        .apply();

                view.setEditButtonEnabled(true);
                view.showSuccessMessage("Hồ sơ đã được cập nhật");
                setEditMode(false);
            }

            @Override
            public void onFailure(String message) {
                view.setEditButtonEnabled(true);
                setEditMode(true);
                view.showErrorMessage(message);
            }
        });
    }

    /**
     * Handle logout
     */
    public void performLogout() {
        view.performLogout();
    }

    /**
     * Navigate back to settings
     */
    public void navigateToSettings() {
        view.navigateToSettings();
    }

    public boolean isEditMode() {
        return isEditMode;
    }
}

