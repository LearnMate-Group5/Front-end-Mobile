package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

public class ApiError {
    @SerializedName("code") public String code;
    @SerializedName("description") public String description;
}
