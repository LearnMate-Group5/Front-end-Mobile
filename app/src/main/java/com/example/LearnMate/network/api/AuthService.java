package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.*;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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

    // Lấy thông tin profile và roles của user hiện tại
    @GET("/api/User/roles/me")
    Call<UserRolesMeResponse> getUserRolesMe();

    // Lấy thông tin profile người dùng
    @PUT("/api/User/{userId}/activation")
    Call<ApiResult<Object>> setActivation(@Path("userId") String userId,
                                          @Query("isActive") boolean isActive);

    @Multipart
    @PUT("/api/User/profile")
    Call<ApiResult<Object>> updateUserProfile(
            @Part("Name") RequestBody name,
            @Part("Email") RequestBody email,
            @Part("DateOfBirth") RequestBody dateOfBirth,
            @Part("Gender") RequestBody gender,
            @Part("PhoneNumber") RequestBody phoneNumber,
            @Part MultipartBody.Part avatarFile);

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
