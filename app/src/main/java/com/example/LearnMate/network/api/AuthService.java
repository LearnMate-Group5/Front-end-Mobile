package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthService {

    @POST("/api/User/login")
    Call<ApiResult<AuthPayload>> login(@Body LoginUserCommand body);

    @POST("/api/User/register")
    Call<ApiResult<AuthPayload>> register(@Body RegisterUserCommand body);

    @POST("/api/User/login/firebase")
    Call<ApiResult<AuthPayload>> loginWithFirebase(@Body FirebaseLoginRequest body);

    @POST("/api/User/refresh-token")
    Call<ApiResult<AuthPayload>> refresh(@Body RefreshTokenCommand body);

    @PUT("/api/User/role")
    Call<ApiResult<Object>> updateRole(@Query("userId") String userId,
                                       @Query("roleName") String roleName);

    @GET("/api/User/read")
    Call<ApiResult<Object>> readUsers(); // tuỳ server, bạn có thể đổi sang kiểu phù hợp

    @GET("/api/User/health")
    Call<Object> health();

    // Lấy thông tin profile người dùng
    @PUT("/api/User/{userId}/activation")
    Call<ApiResult<Object>> setActivation(@Path("userId") String userId,
                                          @Query("isActive") boolean isActive);

    @PUT("/api/User/{userId}/profile")
    Call<ApiResult<Object>> updateUserProfile(@Path("userId") String userId,
                                              @Body UpdateUserProfileRequest body);

    // Forgot Password APIs
    @POST("/api/User/users/password/forgot")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest body);

    @GET("/api/User/users/password/verify")
    Call<VerifyTokenResponse> verifyToken(@Query("uid") String uid, @Query("token") String token);

    @GET("/api/User/users/password/otp/verify")
    Call<VerifyOtpResponse> verifyOtp(@Query("uid") String uid, @Query("otp") String otp);

    @POST("/api/User/users/password/reset")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordCommand body);
}
