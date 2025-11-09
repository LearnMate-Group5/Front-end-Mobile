package com.example.LearnMate.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.LearnMate.model.AuthModel;
import com.example.LearnMate.network.dto.AuthPayload;
import com.example.LearnMate.view.LoginView;

public class LoginPresenter {
    private static final String TAG = "LoginPresenter";

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

                // Load subscription ngay sau khi login thành công
                com.example.LearnMate.managers.SubscriptionManager.getInstance(appContext).loadSubscriptionFromAPI();

                view.showSuccessMessage("Login successful");
                view.navigateToHome();
            }

            @Override public void onFailure(String message) {
                view.showErrorMessage(message);
            }
        });
    }

    public void performFirebaseLogin(String idToken) {
        Log.d(TAG, "performFirebaseLogin called");
        Log.d(TAG, "Received Firebase ID Token: " + idToken);
        
        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "Firebase ID token is empty");
            view.showErrorMessage("Firebase ID token is empty");
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
                String email = jsonPayload.optString("email", "");
                String name = jsonPayload.optString("name", "");
                String userId = jsonPayload.optString("user_id", "");
                
                Log.d(TAG, "Parsed email: " + email);
                Log.d(TAG, "Parsed name: " + name);
                Log.d(TAG, "Parsed user_id: " + userId);
                
                // Lưu Firebase token và user info
                SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                sp.edit()
                        .putString("token", idToken)
                        .putString("user_id", userId)
                        .putString("user_email", email)
                        .putString("user_name", name)
                        .putBoolean("is_logged_in", true)
                        .putBoolean("is_firebase_login", true)
                        .apply();

                // Load subscription ngay sau khi login thành công
                com.example.LearnMate.managers.SubscriptionManager.getInstance(appContext).loadSubscriptionFromAPI();

                Log.d(TAG, "Successfully saved user data to SharedPreferences");
                view.showSuccessMessage("Firebase login successful");
                view.navigateToHome();
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
                .putBoolean("is_logged_in", true)
                .putBoolean("is_firebase_login", true)
                .apply();

        Log.d(TAG, "Fallback: Successfully saved basic token data");
        view.showSuccessMessage("Firebase login successful");
        view.navigateToHome();

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

                view.showSuccessMessage("Firebase login successful");
                view.navigateToHome();
            }

            @Override
            public void onFailure(String message) {
                view.showErrorMessage("Firebase login failed: " + message);
            }
        });
        */
    }

    public void onSignupClicked() { view.navigateToSignup(); }
}
