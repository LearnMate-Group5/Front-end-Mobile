package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LoginResponse {

    @SerializedName("value")
    private ValueData value;

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("isFailure")
    private boolean isFailure;

    @SerializedName("error")
    private ErrorDetails error;

    // --- Getters ---

    public ValueData getValue() {
        return value;
    }

    public boolean isSuccess() { // This method fixes the compilation error
        return isSuccess;
    }

    public boolean isFailure() {
        return isFailure;
    }

    public ErrorDetails getError() {
        return error;
    }

    // --- Nested Classes for JSON structure ---

    public static class ValueData {
        @SerializedName("accessToken")
        private String accessToken;

        @SerializedName("refreshToken")
        private String refreshToken;

        @SerializedName("expiresAt")
        private String expiresAt;

        @SerializedName("user")
        private UserData user;

        // Getters for ValueData
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getExpiresAt() { return expiresAt; }
        public UserData getUser() { return user; }
    }

    public static class UserData {
        @SerializedName("userId")
        private String userId;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("roles")
        private List<String> roles;

        // Getters for UserData
        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public List<String> getRoles() { return roles; }
    }

    public static class ErrorDetails {
        @SerializedName("code")
        private String code;

        @SerializedName("description")
        private String description;

        // Getters for ErrorDetails
        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}

