package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model cho User Registration
 * POST /api/User/register
 */
public class RegisterUserCommand {
    @SerializedName("fullName")
    public String fullName;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("password")
    public String password;
    
    @SerializedName("dateOfBirth")
    public String dateOfBirth;  // Optional: ISO 8601 format "2025-11-07T11:18:25.379Z"
    
    @SerializedName("gender")
    public String gender;  // Optional
    
    @SerializedName("phoneNumber")
    public String phoneNumber;  // Optional
    
    // Constructor với required fields only
    public RegisterUserCommand(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        // Optional fields sẽ là null - backend sẽ handle
    }
    
    // Constructor với all fields
    public RegisterUserCommand(String fullName, String email, String password, 
                              String dateOfBirth, String gender, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
}
