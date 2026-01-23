package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model cho GET /api/User/roles/me
 */
public class UserRolesMeResponse {
    @SerializedName("userId")
    public String userId;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("dateOfBirth")
    public String dateOfBirth;
    
    @SerializedName("gender")
    public String gender;
    
    @SerializedName("phoneNumber")
    public String phoneNumber;
    
    @SerializedName("status")
    public String status;
    
    @SerializedName("isVerified")
    public Boolean isVerified;
    
    @SerializedName("isActive")
    public Boolean isActive;
    
    @SerializedName("avatarUrl")
    public String avatarUrl;  // Base64 encoded image
    
    @SerializedName("isPremium")
    public Boolean isPremium;
    
    @SerializedName("providerName")
    public String providerName;
    
    @SerializedName("createdAt")
    public String createdAt;
    
    @SerializedName("updatedAt")
    public String updatedAt;
    
    @SerializedName("roles")
    public List<String> roles;
}

