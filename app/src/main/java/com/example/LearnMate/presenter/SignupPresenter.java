package com.example.LearnMate.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.LearnMate.model.AuthModel;
import com.example.LearnMate.network.dto.AuthPayload;
import com.example.LearnMate.view.SignupView;

public class SignupPresenter {
    private static final String TAG = "SignupPresenter";

    private final SignupView view;
    private final Context appContext;
    private final AuthModel model;

    public SignupPresenter(SignupView view, Context context) {
        this.view = view;
        this.appContext = context.getApplicationContext();
        this.model = new AuthModel(appContext);
    }

    public void performSignup(String email, String password, String confirmPassword, String fullName) {
        performSignup(email, password, confirmPassword, fullName, null, null, null);
    }

    public void performSignup(String email, String password, String confirmPassword, String fullName, 
                            String phoneNumber, String dateOfBirth, String gender) {
        if (fullName == null || fullName.isEmpty()
                || email == null || email.isEmpty()
                || password == null || password.isEmpty()) {
            view.showSignupError("Please fill all required fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            view.showSignupError("Passwords do not match");
            return;
        }

        model.register(fullName, email, password, phoneNumber, dateOfBirth, gender, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(AuthPayload payload) {
                view.showSignupSuccess("Đăng ký thành công! Vui lòng đăng nhập.");
                view.navigateToLogin();
            }

            @Override
            public void onFailure(String message) {
                view.showSignupError(message);
            }
        });
    }

    public void performFirebaseSignup(String idToken, String email, String displayName) {
        Log.d(TAG, "performFirebaseSignup called");
        Log.d(TAG, "Received Firebase ID Token: " + idToken);
        Log.d(TAG, "Received Email: " + email);
        Log.d(TAG, "Received Display Name: " + displayName);
        
        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "Firebase ID token is empty");
            view.showSignupError("Firebase ID token is empty");
            return;
        }

        // Tạm thời bypass backend API và lưu Firebase token trực tiếp
        // TODO: Khi backend API hoạt động đúng, uncomment phần dưới và xóa phần này
        try {
            Log.d(TAG, "Parsing Firebase ID token...");
            // Parse Firebase ID token để lấy thông tin user
            String[] parts = idToken.split("\\.");
            Log.d(TAG, "Token parts count: " + parts.length);
            
            if (parts.length >= 2) {
                String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
                Log.d(TAG, "Token payload: " + payload);
                
                // Parse JSON để lấy email, name và user_id
                org.json.JSONObject jsonPayload = new org.json.JSONObject(payload);
                String userEmail = jsonPayload.optString("email", email);
                String userName = jsonPayload.optString("name", displayName);
                String userId = jsonPayload.optString("user_id", "");
                
                Log.d(TAG, "Parsed email: " + userEmail);
                Log.d(TAG, "Parsed name: " + userName);
                Log.d(TAG, "Parsed user_id: " + userId);
                
                // Lưu Firebase token và user info
                SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("token", idToken)
                        .putString("user_id", userId)
                        .putString("user_email", userEmail)
                        .putString("user_name", userName)
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_firebase_login", true)
                        .apply();

                // Load subscription và user profile ngay sau khi signup thành công
                com.example.LearnMate.managers.SubscriptionManager.getInstance(appContext).loadSubscriptionFromAPI();
                com.example.LearnMate.managers.UserManager.getInstance(appContext).loadUserProfileFromAPI();

                Log.d(TAG, "Successfully saved user data to SharedPreferences");
                view.showSignupSuccess("Firebase signup successful");
                navigateToHome();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Firebase token", e);
            // Fallback nếu parse token thất bại
        }

        // Fallback: lưu token cơ bản
        Log.d(TAG, "Using fallback method to save token");
        SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        sp.edit()
                .putString("token", idToken)
                .putString("user_id", "firebase_user_id") // Fallback user_id
                .putString("user_email", email)
                .putString("user_name", displayName)
                .putBoolean("is_logged_in", true)
                .putBoolean("is_firebase_login", true)
                .apply();

        // Load subscription và user profile ngay sau khi signup thành công
        com.example.LearnMate.managers.SubscriptionManager.getInstance(appContext).loadSubscriptionFromAPI();
        com.example.LearnMate.managers.UserManager.getInstance(appContext).loadUserProfileFromAPI();

        Log.d(TAG, "Fallback: Successfully saved basic token data");
        view.showSignupSuccess("Firebase signup successful");
        navigateToHome();

        /* TODO: Uncomment khi backend API hoạt động đúng
        model.loginWithFirebase(idToken, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(AuthPayload payload) {
                // Lưu token + user info từ Firebase
                SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("token", payload.accessToken)
                        .putString("refresh_token", payload.refreshToken)
                        .putString("expires_at", payload.expiresAt)
                        .putString("user_id", payload.user != null ? payload.user.userId : null)
                        .putString("user_name", payload.user != null ? payload.user.name : null)
                        .putString("user_email", payload.user != null ? payload.user.email : null)
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_firebase_login", true)
                        .apply();

                view.showSignupSuccess("Firebase signup successful");
                navigateToHome();
            }

            @Override
            public void onFailure(String message) {
                view.showSignupError("Firebase signup failed: " + message);
            }
        });
        */
    }

    private void navigateToHome() {
        // Navigate to the main screen of the app
        android.content.Intent intent = new android.content.Intent(appContext, com.example.LearnMate.HomeActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
    }

    public void onLoginClicked() {
        view.navigateToLogin();
    }
}
