package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Forgot Password
 * POST /api/User/users/password/forgot
 */
public class ForgotPasswordResponse {
    @SerializedName("userId")
    public String userId;
    
    @SerializedName("token")
    public String token;
}

