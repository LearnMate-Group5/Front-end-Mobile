package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AuthPayload {
    @SerializedName("accessToken") public String accessToken;
    @SerializedName("refreshToken") public String refreshToken;
    @SerializedName("expiresAt") public String expiresAt;

    @SerializedName("user") public UserInfo user;

    public static class UserInfo {
        @SerializedName("userId") public String userId;
        @SerializedName("name") public String name;
        @SerializedName("email") public String email;
        @SerializedName("roles") public List<String> roles;
    }
}
