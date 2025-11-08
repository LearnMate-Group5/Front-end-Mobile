package com.example.LearnMate.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthModel {

    private final AuthService auth;

    public AuthModel(Context appContext) {
        this.auth = RetrofitClient.getAuthService(appContext);
    }

    public void login(@NonNull String email,
                      @NonNull String password,
                      @NonNull AuthCallback cb) {
        auth.login(new LoginUserCommand(email, password)).enqueue(new Callback<ApiResult<AuthPayload>>() {
            @Override public void onResponse(Call<ApiResult<AuthPayload>> call, Response<ApiResult<AuthPayload>> res) {
                if (!res.isSuccessful() || res.body() == null || !res.body().isSuccess || res.body().value == null) {
                    cb.onFailure("Login failed");
                    return;
                }
                cb.onSuccess(res.body().value);
            }
            @Override public void onFailure(Call<ApiResult<AuthPayload>> call, Throwable t) {
                cb.onFailure(t.getMessage() == null ? "Network error" : t.getMessage());
            }
        });
    }

    public void loginWithFirebase(@NonNull String idToken,
                                 @NonNull AuthCallback cb) {
        auth.loginWithFirebase(new FirebaseLoginRequest(idToken)).enqueue(new Callback<ApiResult<AuthPayload>>() {
            @Override
            public void onResponse(Call<ApiResult<AuthPayload>> call, Response<ApiResult<AuthPayload>> res) {
                if (!res.isSuccessful() || res.body() == null || !res.body().isSuccess || res.body().value == null) {
                    cb.onFailure("Firebase login failed");
                    return;
                }
                cb.onSuccess(res.body().value);
            }

            @Override
            public void onFailure(Call<ApiResult<AuthPayload>> call, Throwable t) {
                cb.onFailure(t.getMessage() == null ? "Network error" : t.getMessage());
            }
        });
    }

    public void register(@NonNull String fullName,
                         @NonNull String email,
                         @NonNull String password,
                         @NonNull AuthCallback cb) {
        register(fullName, email, password, null, null, null, cb);
    }

    public void register(@NonNull String fullName,
                         @NonNull String email,
                         @NonNull String password,
                         String phoneNumber,
                         String dateOfBirth,
                         String gender,
                         @NonNull AuthCallback cb) {
        auth.register(new RegisterUserCommand(fullName, email, password, dateOfBirth, gender, phoneNumber))
            .enqueue(new Callback<ApiResult<AuthPayload>>() {
            @Override public void onResponse(Call<ApiResult<AuthPayload>> call, Response<ApiResult<AuthPayload>> res) {
                // Register endpoint returns isSuccess:true but no value field
                // Success is determined by HTTP 200 and isSuccess flag
                if (!res.isSuccessful() || res.body() == null || !res.body().isSuccess) {
                    String errorMsg = "Đăng ký thất bại";
                    if (res.body() != null && res.body().error != null && res.body().error.description != null) {
                        errorMsg = res.body().error.description;
                    }
                    cb.onFailure(errorMsg);
                    return;
                }
                // Registration successful - pass null payload since register doesn't return user data
                cb.onSuccess(null);
            }
            @Override public void onFailure(Call<ApiResult<AuthPayload>> call, Throwable t) {
                cb.onFailure(t.getMessage() == null ? "Lỗi kết nối" : t.getMessage());
            }
        });
    }

    public void updateProfile(@NonNull String userId,
                              @NonNull String name,
                              @NonNull String email,
                              String avatarUrl,
                              @NonNull ProfileCallback cb) {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(name, email, avatarUrl != null ? avatarUrl : "");
        auth.updateUserProfile(userId, request).enqueue(new Callback<ApiResult<Object>>() {
            @Override
            public void onResponse(Call<ApiResult<Object>> call, Response<ApiResult<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                    cb.onSuccess(name, email, avatarUrl != null ? avatarUrl : "");
                } else {
                    String msg = "Cập nhật thất bại";
                    if (response.body() != null && response.body().error != null
                            && response.body().error.description != null) {
                        msg = response.body().error.description;
                    }
                    cb.onFailure(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResult<Object>> call, Throwable t) {
                cb.onFailure("Lỗi mạng: " + (t.getMessage() == null ? "" : t.getMessage()));
            }
        });
    }

    public interface AuthCallback {
        void onSuccess(AuthPayload payload);
        void onFailure(String message);
    }

    public interface ProfileCallback {
        void onSuccess(String name, String email, String avatarUrl);
        void onFailure(String message);
    }
}
