package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Response DTO for Category
 */
public class CategoryResponse {
    @SerializedName("categoryId")
    private UUID categoryId;
    
    @SerializedName("name")
    private String name;
    
    // Getters and Setters
    public UUID getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

