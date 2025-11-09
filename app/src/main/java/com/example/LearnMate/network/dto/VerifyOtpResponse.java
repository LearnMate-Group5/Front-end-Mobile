package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Verify OTP
 * GET /api/User/users/password/otp/verify
 */
public class VerifyOtpResponse {
    @SerializedName("userId")
    public String userId;
    
    @SerializedName("expiresAt")
    public String expiresAt;
}

