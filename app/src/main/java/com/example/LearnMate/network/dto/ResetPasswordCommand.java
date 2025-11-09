package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model cho Reset Password
 * POST /api/User/users/password/reset
 */
public class ResetPasswordCommand {
    @SerializedName("email")
    public String email;
    
    @SerializedName("token")
    public String token;
    
    @SerializedName("otp")
    public String otp;
    
    @SerializedName("newPassword")
    public String newPassword;
    
    @SerializedName("confirmNewPassword")
    public String confirmNewPassword;
    
    public ResetPasswordCommand(String email, String token, String otp, 
                                String newPassword, String confirmNewPassword) {
        this.email = email;
        this.token = token;
        this.otp = otp;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }
}

