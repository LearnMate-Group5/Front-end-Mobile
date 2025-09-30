package com.example.learnmate_frontend.network;
import com.example.learnmate_frontend.model.GoogleLoginRequest;
import com.example.learnmate_frontend.model.User;
import com.example.learnmate_frontend.model.LoginRequest;
import com.example.learnmate_frontend.model.LoginResponse;
import com.example.learnmate_frontend.model.RegisterRequest;
import com.example.learnmate_frontend.model.GenericResponse;
import com.google.gson.JsonObject;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
public interface ApiService {
    @POST("api/signup")
    Call<GenericResponse> register(@Body RegisterRequest registerRequest);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/login/google-login")
    Call<LoginResponse> googleLogin(@Body GoogleLoginRequest googleLoginRequest);

    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") int id, @Header("Authorization") String token);

    @GET("api/user/{id}")
    Call<User> getCurrentUser(@Header("Authorization") String token);
}
