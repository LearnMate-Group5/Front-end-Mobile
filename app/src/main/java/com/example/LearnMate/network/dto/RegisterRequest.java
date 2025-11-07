package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model cho User Registration
 * POST /api/User/register
 */
public class RegisterRequest {
    @SerializedName("fullName")
    public String fullName;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("password")
    public String password;
    
    @SerializedName("dateOfBirth")
    public String dateOfBirth;
    
    @SerializedName("gender")
    public String gender;
    
    @SerializedName("phoneNumber")
    public String phoneNumber;

    public RegisterRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
    
    public RegisterRequest(String fullName, String email, String password, 
                          String dateOfBirth, String gender, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
}
