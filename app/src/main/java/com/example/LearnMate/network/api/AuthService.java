package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
}
