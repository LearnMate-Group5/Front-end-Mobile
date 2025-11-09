package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Category từ API GET /api/Book/Categories
 */
public class CategoryResponse {
    @SerializedName("categoryId")
    public String categoryId;

    @SerializedName("name")
    public String name;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;
}

