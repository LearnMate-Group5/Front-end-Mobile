package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model cho Forgot Password
 * POST /api/User/users/password/forgot
 */
public class ForgotPasswordRequest {
    @SerializedName("email")
    public String email;
    
    public ForgotPasswordRequest(String email) {
        this.email = email;
    }
}

