package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Reset Password
 * POST /api/User/users/password/reset
 */
public class ResetPasswordResponse {
    @SerializedName("message")
    public String message;
}

