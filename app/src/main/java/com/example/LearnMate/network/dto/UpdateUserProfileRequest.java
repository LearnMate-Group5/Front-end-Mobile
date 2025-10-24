// com/example/LearnMate/network/dto/UpdateUserProfileRequest.java
package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateUserProfileRequest {
    @SerializedName("name")      public String name;
    @SerializedName("email")     public String email;
    @SerializedName("avatarUrl") public String avatarUrl;

    public UpdateUserProfileRequest(String name, String email, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }
}
