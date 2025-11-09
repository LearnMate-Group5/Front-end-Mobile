package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Verify Token
 * GET /api/User/users/password/verify
 */
public class VerifyTokenResponse {
    @SerializedName("userId")
    public String userId;
    
    @SerializedName("expiresAt")
    public String expiresAt;
}

